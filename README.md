## Opções principais
| Opção | Descrição |
| --- | --- |
| BRUXA (POÇÕES) | Adiciona exatamente 2 poções a cada bruxa. Cada poção é sorteada de forma independente, com 1/9 de chance para cada opção: Resistência ao Fogo, Agilidade, Salto, Força, Visão Noturna ou Respiração Aquática por 8 minutos; Queda Lenta por 4 minutos; Veneno ou Regeneração por 22 segundos. Os dois sorteios podem coincidir. As poções são preservadas por BRUXA (TUDO). |
| CREEPER (TNT) | Adiciona exatamente 1 TNT a 10% dos drops de creepers. |
| ARANHA (TEIA) | Adiciona exatamente 1 teia a 15% dos drops de aranhas. |
| SAQUEADOR (RIQUEZAS) | Adiciona exatamente 2 itens aos drops de saqueadores. Cada item é sorteado de forma independente: 50% de chance de Esmeralda, 40% de Lingote de Ouro e 10% de Diamante. Os dois sorteios podem coincidir. |
| ESQUELETO (ESPECTRAL) | Adiciona exatamente 1 flecha espectral a 20% dos drops de esqueletos. |
| BRUXA (TUDO) | Remove todos os drops de itens das bruxas, preservando a recompensa adicionada por BRUXA (POÇÕES). |
| ESQUELETO (ARMADURAS) | Remove arcos e qualquer item com o componente `EQUIPPABLE` dos drops de esqueletos, incluindo unidades encantadas, danificadas ou renomeadas. Equipamentos marcados pelo jogo para preservação continuam sendo soltos. |
| SAQUEADOR (BESTA) | Remove bestas dos drops de saqueadores, incluindo unidades encantadas, danificadas, renomeadas ou carregadas. Equipamentos marcados pelo jogo para preservação continuam sendo soltos. |
| ZUMBI (ARMADURAS) | Remove dos drops de zumbis qualquer item com o componente `EQUIPPABLE`, espada de ferro, lança de ferro, pá de ferro, cenoura, batata, batata assada e lingote de ferro. Equipamentos marcados pelo jogo para preservação continuam sendo soltos. |

## Opções globais
| Opção | Descrição |
| --- | --- |
| AMEAÇA CONSTANTE | Reduz o intervalo das patrulhas de 12.000 para 800 ticks (10 minutos para 40 segundos) e sua variação de 1.200 para 80 ticks (1 minuto para 4 segundos), mantendo as demais condições vanilla de surgimento. Dobra o dano causado diretamente por invasores. Capitães fora de invasões que já soltariam 1 Frasco Sombrio passam a soltar 2, cada um com Mau Presságio independente de I a V. |
| CAVALO ESTACIONADO | Imobiliza montarias da família dos cavalos enquanto estiverem seladas e sem passageiro. O movimento normal retorna quando recebem um passageiro ou perdem a sela. |
| COELHOS CERCADOS | Impede um coelho de saltar quando uma cerca da tag vanilla `#minecraft:fences` estiver à frente do movimento, até 1,35 bloco adiante e 0,8 bloco para cada lado. A busca cobre 5 × 5 blocos no plano horizontal e 2 níveis verticais, do Y atual ao bloco abaixo; ao detectar a cerca, cancela o salto e interrompe a navegação. |
| ENDERMAN BLOQUEADO | Impede endermen de iniciar a ação de pegar blocos do mundo. Blocos que já estejam sendo carregados permanecem com o enderman e ainda podem ser colocados normalmente. |
| ELYTRA RÁPIDA | Alterna entre o peitoral equipado e uma elytra do inventário ao pressionar Grave Accent, a tecla antes do 1. Se uma elytra estiver equipada, troca por um peitoral. Entre vários itens do tipo necessário, usa o que tiver menos durabilidade restante. |
| MODO AUSENTE | Abre o menu de pausa e interrompe o mundo após 15 segundos sem movimento do mouse. Funciona somente em mundos singleplayer que não estejam abertos para LAN e apenas enquanto a janela do Minecraft estiver em foco. |
| VIDA CAVALAR | Acrescenta ao tooltip do Jade, abaixo da velocidade, a vida atual e a vida máxima do cavalo em pontos de vida, arredondadas para duas casas decimais. A opção pode ser configurada sem o Jade, mas a informação só aparece quando ele estiver instalado. |

## Padrões e configurações existentes

Na primeira instalação, todas as opções principais começam ativadas e todas as opções globais começam desativadas. Atualizações e migrações preservam as escolhas explicitamente salvas. Novas opções seguem o padrão de seu tipo, e campos ausentes usam esse mesmo padrão sem redefinir escolhas válidas existentes.
