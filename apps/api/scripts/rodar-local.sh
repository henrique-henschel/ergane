#!/usr/bin/env bash
# Sobe a API no perfil local com um par de chaves RSA só de desenvolvimento.
# Não altera a configuração de JWT: apenas preenche as propriedades
# ergane.security.jwt.* pelas variáveis de ambiente ERGANE_SECURITY_JWT_*.
# Variáveis já definidas no terminal têm prioridade sobre os valores abaixo.
# As chaves ficam em apps/api/.jwt-local/ (ignorada pelo git e pelo Docker).
#
# Uso (na pasta apps/api; no Windows, pelo Git Bash): ./scripts/rodar-local.sh
set -euo pipefail
cd "$(dirname "$0")/.."

DIR=.jwt-local
if [ ! -f "$DIR/privada.pem" ] || [ ! -f "$DIR/publica.pem" ]; then
  echo "Gerando par de chaves RSA de desenvolvimento em apps/api/$DIR ..."
  mkdir -p "$DIR"
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$DIR/privada.pem" 2>/dev/null
  openssl pkey -in "$DIR/privada.pem" -pubout -out "$DIR/publica.pem"
fi

export ERGANE_SECURITY_JWT_ISSUER="${ERGANE_SECURITY_JWT_ISSUER:-ergane-local}"
export ERGANE_SECURITY_JWT_AUDIENCE="${ERGANE_SECURITY_JWT_AUDIENCE:-ergane-api}"
export ERGANE_SECURITY_JWT_PRIVATEKEY="${ERGANE_SECURITY_JWT_PRIVATEKEY:-file:$DIR/privada.pem}"
export ERGANE_SECURITY_JWT_PUBLICKEY="${ERGANE_SECURITY_JWT_PUBLICKEY:-file:$DIR/publica.pem}"

exec ./mvnw spring-boot:run "$@"
