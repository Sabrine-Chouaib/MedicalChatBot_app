#!/bin/bash
# ===============================
# MINI RAG VECTORIEL COMPLET
# Ollama (nomic-embed-text) + Pinecone
# ===============================

# 1️⃣ Variables Pinecone
PINECONE_API_KEY="pcsk_4wArr8_6a1LrjJDZbsmksMzNZDkFpnBrs2gss918tgexyyvQgQSW1aUVGqStvYCdUSqiLg"
PINECONE_HOST="https://medical-chatbot-ollama-y4ojuuz.svc.aped-4627-b74a.pinecone.io"

# 2️⃣ Fonction pour générer un embedding via l'API locale Ollama
generate_embedding() {
    local TEXT="$1"
    EMBEDDING_JSON=$(curl -s http://localhost:11434/api/embeddings \
        -H "Content-Type: application/json" \
        -d "{
              \"model\": \"nomic-embed-text\",
              \"prompt\": \"$TEXT\"
            }")
    # Extraire le vecteur depuis JSON et convertir en nombre
    EMBEDDING=$(echo "$EMBEDDING_JSON" | jq -c '.embedding | map(tonumber)')

    echo "$EMBEDDING"
}

# 3️⃣ Texte à stocker
TEXT_TO_STORE="Bonjour"
echo "Texte à stocker : $TEXT_TO_STORE"

# 4️⃣ Générer l'embedding réel
EMBEDDING=$(generate_embedding "$TEXT_TO_STORE")
DIM=$(echo "$EMBEDDING" | jq 'length')
echo "Embedding généré avec $DIM dimensions"

# 5️⃣ Upsert du vecteur dans Pinecone
VECTOR_ID="rag-$(date +%s)"  # id unique basé sur timestamp

UPsert_BODY=$(jq -n \
    --arg id "$VECTOR_ID" \
    --argjson values "$EMBEDDING" \
    --arg text "$TEXT_TO_STORE" \
    '{vectors: [{id: $id, values: $values, metadata: {text: $text}}]}')

echo "➡️ Upsert du vecteur..."
curl -s -X POST "$PINECONE_HOST/vectors/upsert" \
    -H "Api-Key: $PINECONE_API_KEY" \
    -H "Content-Type: application/json" \
    -d "$UPsert_BODY" | jq

# 6️⃣ Question pour recherche
QUESTION="Quel est le test de Bonjour"
echo -e "\nQuestion : $QUESTION"

# 7️⃣ Générer l'embedding de la question
QUERY_EMBEDDING=$(generate_embedding "$QUESTION")

# 8️⃣ Préparer le body de query
QUERY_BODY=$(jq -n \
    --argjson vector "$QUERY_EMBEDDING" \
    '{vector: $vector, topK: 1, includeMetadata: true}')

echo "➡️ Recherche du vecteur le plus proche..."
curl -s -X POST "$PINECONE_HOST/query" \
    -H "Api-Key: $PINECONE_API_KEY" \
    -H "Content-Type: application/json" \
    -d "$QUERY_BODY" | jq

# 9️⃣ Fin
echo -e "\n✅ Test RAG vectoriel terminé !"
