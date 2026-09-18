# Migração para Minecraft 26.3 — ControlDrop

Data: 2026-09-18. Build final concluído com Temurin 25.0.3+9 e Gradle 9.6.0.

Dependências: Loader 0.19.5, Fabric API 0.160.7+26.3, Mod Menu 21.0.0-beta.1 e Jade 26.3.1+fabric (opcional). Loom 1.17.14 e versão do mod 1.0 preservados.

Adaptações: RedstoneWireBlock; PushReaction.POPPED; alvo interno Enderman; leitura de teclas sem parâmetro Window; botões do mouse pela constante InputConstants; assinatura de tooltip com espaçamento adicional desativado; devolução de itens com Prediction.SERVER_ONLY.

Validação: clean build --warning-mode all bem-sucedido; metadados do JAR conferidos; revisão estática dos alvos dos 23 mixins, descritores explícitos, chamadas redirecionadas e constantes de patrulhas. Não houve inicialização do Minecraft nem teste de aplicação dos mixins em execução. A integração por reflexão com Inventory Profiles Next depende de teste com sua versão compatível instalada.

Artefato: build/libs/ControlDrop-1.0.jar. Instalação adiada por acordo com o usuário até a atualização da NEBULOSA. Nenhum commit, push ou release.

Teste manual pendente: abrir configurações, selecionar e rolar ambas as listas, conferir tooltips em GUI Scale 2x; testar atalhos, desfazer exclusão com inventário cheio, cabeça de aranha, redstone oculta, drops, patrulhas, durabilidade e impedimento de coleta de blocos por Endermen.

AGENTS.md preservado; suas versões compartilhadas ainda descrevem 26.2 e precisam de atualização explícita do modelo comum.

Fontes: https://www.fabricmc.net/2026/09/15/263.html ; https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3 ; https://piston-meta.mojang.com/mc/game/version_manifest_v2.json ; APIs oficiais de distribuição Fabric e Modrinth e classes/dados 26.3 resolvidos pelo Loom.

Auditoria estática adicional do JAR: 53 verificações de seletores, parâmetros de callbacks, campos, instruções e constantes; nenhuma divergência nos pontos verificados. Integrações opcionais externas excluídas desta auditoria. Todos os JSONs empacotados passaram na verificação de sintaxe.

Logs desta etapa: build/reports/migration-26.3/build.log e static-audit.txt. JSONs empacotados verificados sintaticamente.
