#!/bin/bash
# ===============================
# TEST PINECONE AVEC VECTEUR 768D
# ===============================

# Variables Pinecone
PINECONE_API_KEY="pcsk_4wArr8_6a1LrjJDZbsmksMzNZDkFpnBrs2gss918tgexyyvQgQSW1aUVGqStvYCdUSqiLg"
PINECONE_HOST="https://medical-chatbot-ollama-y4ojuuz.svc.aped-4627-b74a.pinecone.io"

# 1️⃣ Générer un vecteur aléatoire de 768 dimensions
VECTOR=$(for i in $(seq 1 768); do 
    printf "%.6f," $(awk -v seed=$RANDOM 'BEGIN{srand(seed); print rand()}') 
done | sed 's/,$//')  # supprime la dernière virgule

# 2️⃣ Upsert dans Pinecone
VECTOR_ID="test-768-$(date +%s)"

UPsert_BODY=$(jq -n \
    --arg id "$VECTOR_ID" \
    --arg text "Test Pinecone 768D" \
    --argjson values "[$VECTOR]" \
    '{vectors: [{id: $id, values: $values, metadata: {text: $text}}]}')

echo "➡️ Upsert du vecteur dans Pinecone..."
curl -s -X POST "$PINECONE_HOST/vectors/upsert" \
    -H "Api-Key: $PINECONE_API_KEY" \
    -H "Content-Type: application/json" \
    -d "$UPsert_BODY" | jq

# 3️⃣ Query pour retrouver le vecteur
QUERY_BODY=$(jq -n \
    --argjson vector "[$VECTOR]" \
    '{vector: $vector, topK: 1, includeMetadata: true}')

echo -e "\n➡️ Recherche du vecteur le plus proche..."
curl -s -X POST "$PINECONE_HOST/query" \
    -H "Api-Key: $PINECONE_API_KEY" \
    -H "Content-Type: application/json" \
    -d "$QUERY_BODY" | jq

echo -e "\n✅ Test Pinecone 768D terminé !"
