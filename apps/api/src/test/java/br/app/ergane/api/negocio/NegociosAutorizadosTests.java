package br.app.ergane.api.negocio;

import br.app.ergane.api.compartilhado.security.PropriedadesJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(NegociosAutorizadosTests.ChavesDeTeste.class)
// As requisições síncronas e os fixtures compartilham a transação revertida ao fim de cada teste.
@Transactional
class NegociosAutorizadosTests {

    private static final String SENHA = "senha-de-teste";
    private static final String CONTEXTO = "/api/sessoes/atual/negocio";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate banco;
    @Autowired
    private PasswordEncoder senhas;
    @Autowired
    private ObjectMapper json;

    @Test
    void exigeAutenticacaoParaListarNegocios() throws Exception {
        mvc.perform(get("/api/negocios"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void listaApenasVinculosAtivosDoTitularMesmoSemContexto() throws Exception {
        Usuario ana = usuario("ana");
        Usuario bruno = usuario("bruno");
        Usuario semAcesso = usuario("sem-acesso");
        UUID padaria = negocio("Padaria");
        UUID mercearia = negocio("Mercearia");
        acesso(ana, padaria, "FUNCIONARIO", true);
        acesso(ana, mercearia, "ADMINISTRADOR", true);
        acesso(ana, negocio("Oficina"), "FUNCIONARIO", false);
        acesso(bruno, negocio("Cafeteria"), "PROPRIETARIO", true);
        String token = login(ana);

        contexto(token).andExpect(jsonPath("$.negocioId").value(nullValue()));
        mvc.perform(get("/api/negocios").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].negocioId", containsInAnyOrder(padaria.toString(), mercearia.toString())))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Padaria", "Mercearia")))
                .andExpect(jsonPath("$[*].papel", containsInAnyOrder("FUNCIONARIO", "ADMINISTRADOR")));

        mvc.perform(get("/api/negocios").header(HttpHeaders.AUTHORIZATION, bearer(login(semAcesso))))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void selecaoRestauraUltimoNegocioEmNovoLoginSomenteSeVinculoEstiverAtivo() throws Exception {
        Usuario ana = usuario("ana");
        UUID padaria = negocio("Padaria");
        UUID mercearia = negocio("Mercearia");
        acesso(ana, padaria, "FUNCIONARIO", true);
        UUID acessoMercearia = acesso(ana, mercearia, "FUNCIONARIO", true);
        String token = login(ana);
        selecionar(token, padaria);
        selecionar(token, mercearia);

        contexto(token).andExpect(jsonPath("$.negocioId").value(mercearia.toString()));
        contexto(login(ana)).andExpect(jsonPath("$.negocioId").value(mercearia.toString()));

        banco.update("UPDATE acessos_negocio SET ativo = false WHERE id = ?", acessoMercearia);

        contexto(token).andExpect(jsonPath("$.negocioId").value(nullValue()));
        contexto(login(ana)).andExpect(jsonPath("$.negocioId").value(nullValue()));
    }

    @Test
    void revogacaoPorPapelEComSenhaPreservaSessaoEOutrosNegocios() throws Exception {
        Usuario proprietaria = usuario("proprietaria");
        Usuario admin = usuario("administrador");
        Usuario funcionario = usuario("funcionario");
        UUID padaria = negocio("Padaria");
        UUID mercearia = negocio("Mercearia");
        UUID acessoProprietaria = acesso(proprietaria, padaria, "PROPRIETARIO", true);
        UUID acessoAdmin = acesso(admin, padaria, "ADMINISTRADOR", true);
        UUID acessoFuncionario = acesso(funcionario, padaria, "FUNCIONARIO", true);
        acesso(funcionario, mercearia, "FUNCIONARIO", true);
        UUID categoriaPadaria = categoria(padaria, "Pães");
        UUID categoriaMercearia = categoria(mercearia, "Cereais");
        String tokenProprietaria = login(proprietaria);
        String tokenAdmin = login(admin);
        String tokenFuncionario = login(funcionario);
        selecionar(tokenFuncionario, padaria);
        categoria(tokenFuncionario, categoriaPadaria).andExpect(status().isOk());
        categoria(tokenFuncionario, categoriaMercearia).andExpect(status().isNotFound());

        revogar(tokenProprietaria, mercearia, acessoFuncionario, SENHA).andExpect(status().isNotFound());
        revogar(tokenAdmin, padaria, acessoProprietaria, SENHA).andExpect(status().isNotFound());
        revogar(tokenAdmin, padaria, acessoFuncionario, "senha-incorreta").andExpect(status().isUnauthorized());
        assertThat(banco.queryForObject("SELECT ativo FROM acessos_negocio WHERE id = ?", Boolean.class,
                acessoFuncionario)).isTrue();
        revogar(tokenAdmin, padaria, acessoFuncionario, SENHA).andExpect(status().isNoContent());
        revogar(tokenProprietaria, padaria, acessoAdmin, SENHA).andExpect(status().isNoContent());

        contexto(tokenFuncionario).andExpect(jsonPath("$.negocioId").value(nullValue()));
        mvc.perform(get("/api/negocios").header(HttpHeaders.AUTHORIZATION, bearer(tokenFuncionario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].negocioId", containsInAnyOrder(mercearia.toString())));
        selecionar(tokenFuncionario, mercearia);
        contexto(tokenFuncionario).andExpect(jsonPath("$.negocioId").value(mercearia.toString()));
        categoria(tokenFuncionario, categoriaPadaria).andExpect(status().isNotFound());
        categoria(tokenFuncionario, categoriaMercearia).andExpect(status().isOk());
    }

    @Test
    void rotacaoRejeitaTokensAnterioresERevogacaoRejeitaTokensAtuais() throws Exception {
        Usuario ana = usuario("ana");
        JsonNode login = credenciais(ana);
        String acessoAnterior = login.get("accessToken").stringValue();
        String refreshAnterior = login.get("refreshToken").stringValue();

        var resposta = renovar(refreshAnterior).andExpect(status().isOk()).andReturn().getResponse();
        JsonNode renovada = json.readTree(resposta.getContentAsString());
        String acessoAtual = renovada.get("accessToken").stringValue();
        String refreshAtual = renovada.get("refreshToken").stringValue();

        assertThat(renovada.get("sessaoId")).isEqualTo(login.get("sessaoId"));
        assertThat(acessoAtual).isNotEqualTo(acessoAnterior);
        assertThat(refreshAtual).isNotEqualTo(refreshAnterior);
        mvc.perform(get(CONTEXTO).header(HttpHeaders.AUTHORIZATION, bearer(acessoAnterior)))
                .andExpect(status().isUnauthorized());
        renovar(refreshAnterior).andExpect(status().isUnauthorized());
        contexto(acessoAtual);
        mvc.perform(post("/api/sessoes/atual/revogar").header(HttpHeaders.AUTHORIZATION, bearer(acessoAtual)))
                .andExpect(status().isOk());
        mvc.perform(get(CONTEXTO).header(HttpHeaders.AUTHORIZATION, bearer(acessoAtual)))
                .andExpect(status().isUnauthorized());
        renovar(refreshAtual).andExpect(status().isUnauthorized());
    }

    private Usuario usuario(String nome) {
        UUID id = UUID.randomUUID();
        String email = nome + "-" + id + "@example.test";
        banco.update("INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, ?, ?, ?)",
                id, nome, email, senhas.encode(SENHA));
        return new Usuario(id, email);
    }

    private UUID negocio(String nome) {
        UUID id = UUID.randomUUID();
        banco.update("INSERT INTO negocios (id, nome) VALUES (?, ?)", id, nome);
        return id;
    }

    private UUID acesso(Usuario usuario, UUID negocio, String papel, boolean ativo) {
        UUID id = UUID.randomUUID();
        banco.update("INSERT INTO acessos_negocio (id, usuario_id, negocio_id, papel, ativo) VALUES (?, ?, ?, ?, ?)",
                id, usuario.id(), negocio, papel, ativo);
        return id;
    }

    private UUID categoria(UUID negocio, String nome) {
        UUID id = UUID.randomUUID();
        banco.update("INSERT INTO categorias (id, negocio_id, nome) VALUES (?, ?, ?)", id, negocio, nome);
        return id;
    }

    private String login(Usuario usuario) throws Exception {
        return credenciais(usuario).get("accessToken").stringValue();
    }

    private JsonNode credenciais(Usuario usuario) throws Exception {
        String corpo = mvc.perform(post("/api/sessoes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}
                                """.formatted(usuario.email(), SENHA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    private ResultActions renovar(String refresh) throws Exception {
        return mvc.perform(post("/api/sessoes/renovacoes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"%s\"}".formatted(refresh)));
    }

    private ResultActions contexto(String token) throws Exception {
        return mvc.perform(get(CONTEXTO).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    private ResultActions categoria(String token, UUID id) throws Exception {
        return mvc.perform(get("/api/categorias/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private void selecionar(String token, UUID negocio) throws Exception {
        mvc.perform(put(CONTEXTO).header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"negocioId\":\"%s\"}".formatted(negocio)))
                .andExpect(status().isOk());
    }

    private ResultActions revogar(String token, UUID negocio, UUID acesso, String senha) throws Exception {
        return mvc.perform(post("/api/negocios/{negocioId}/acessos/{acessoId}/revogar", negocio, acesso)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senha\":\"%s\"}".formatted(senha)));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Usuario(UUID id, String email) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ChavesDeTeste {

        @Bean
        @Primary
        PropriedadesJwt propriedadesJwtDeTeste() throws NoSuchAlgorithmException {
            KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(2048);
            var chaves = gerador.generateKeyPair();
            return new PropriedadesJwt("ergane-teste", "ergane-api-teste",
                    pem("PRIVATE KEY", chaves.getPrivate().getEncoded()),
                    pem("PUBLIC KEY", chaves.getPublic().getEncoded()));
        }

        private ByteArrayResource pem(String tipo, byte[] chave) {
            String dados = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(chave);
            String texto = "-----BEGIN %s-----\n%s\n-----END %s-----\n".formatted(tipo, dados, tipo);
            return new ByteArrayResource(texto.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
