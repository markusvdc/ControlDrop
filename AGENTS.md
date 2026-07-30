# DROP CONTROL — Instruções do ambiente de desenvolvimento

Leia este arquivo antes de alterar, compilar ou testar o projeto.

## Ambiente

- Sistema: Windows com PowerShell.
- Minecraft: `26.2`.
- Fabric Loader: `0.19.3`.
- Fabric API: `0.155.0+26.2`.
- Mod Menu: `20.0.1`.
- Java: `25`.
- JDK: `25.0.3`, versão LTS mais recente adotada pelo projeto, com suporte prolongado.
- JDK usado no desenvolvimento:
  `D:\MARKUS\GAMES\minecraft\java\jdk-25.0.3`

## Build

Antes de executar qualquer operação potencialmente demorada, como build, varredura ampla ou outra tarefa pesada, informe em uma frase por que ela é necessária. Se não houver necessidade técnica, não execute a operação.

Execute o build com o JDK 25 configurado:

```powershell
$env:JAVA_HOME='D:\MARKUS\GAMES\minecraft\java\jdk-25.0.3'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean build --warning-mode all
```

Quando a alteração afetar código, recursos do mod, configuração de build ou o conteúdo do JAR, considere o trabalho concluído somente depois de um build bem-sucedido.

### Exceção para documentação

- Não execute build quando todas as alterações forem exclusivamente de documentação.
- Exemplos: `AGENTS.md`, `README.md` e outros arquivos informativos que não entram no JAR nem afetam o funcionamento do mod.
- Alterações somente de documentação também não exigem instalação automática na instância de teste.
- Se houver qualquer mudança de código, recurso, dependência ou configuração junto com a documentação, execute normalmente o build e a instalação automática.

## Instalação automática na instância de teste

Depois de cada build bem-sucedido:

1. Faça o deploy nas duas instâncias abaixo:
   - `D:\MARKUS\GAMES\minecraft\instances\NEBULOSA6\minecraft\mods`
   - `D:\MARKUS\GAMES\minecraft\instances\NEBULOSA7\minecraft\mods`
2. Resolva e valide que cada destino corresponde exatamente à respectiva pasta autorizada.
3. Localize em cada pasta somente arquivos que correspondam a:
   `dropcontrol-*.jar`
4. Remova somente esses JARs antigos do DROP CONTROL.
5. Copie o novo JAR de `build\libs` para as duas pastas de mods.
6. Confirme que existe exatamente um `dropcontrol-*.jar` em cada destino e que ambos correspondem à versão recém-compilada.

Nunca remova, mova ou substitua outros mods dessas pastas.

## Controle de versão

- Agrupe implementação, refinamentos, correções e acabamentos da mesma entrega em um único commit.
- Considere uma entrega estrutural pronta somente depois de finalizada e validada conforme as regras do projeto.
- Antes de iniciar uma nova implementação estrutural de outra natureza, faça o commit da entrega anterior já concluída.
- Uma simples mudança de assunto ou arquivo não cria, por si só, uma fronteira entre commits; avalie intenção, tamanho e caráter estrutural.
- Pequenos ajustes feitos depois de um commit podem permanecer acumulados até formarem um conjunto coerente, antecederem uma nova entrega estrutural ou o usuário solicitar um commit.
- Não misture uma nova entrega estrutural com outra já concluída e ainda não registrada.
- Escreva as mensagens de commit sempre em inglês.
- Mantenha as mensagens com aproximadamente 45 caracteres. Esse valor é uma referência de tamanho, não um limite máximo; priorize mensagens descritivas com comprimentos visualmente semelhantes.
- Fazer um commit não autoriza um push.
- Execute push somente quando o usuário solicitar explicitamente.

### Releases no GitHub

- Crie toda Release inicialmente como rascunho e publique somente após autorização explícita do usuário.
- Use a tag `v<versão>` e anexe o JAR `dropcontrol-<versão>.jar`.
- Atualize no texto as versões de Minecraft, Fabric Loader, Fabric API e Mod Menu correspondentes à Release.

#### Título

- Escreva em inglês no formato `<emoji> <título temático>`.
- Represente a identidade ou a principal mudança da versão em aproximadamente 30 caracteres.
- Não inclua `DROP CONTROL` nem o número da versão; essas informações já aparecem no repositório e na tag.
- Referência: `💀 Taking Control of Drops` — 24 caracteres.

#### Descrição

- Escreva em inglês, como uma única estrofe com aproximadamente 440 caracteres e sem negrito.
- Depois da estrofe, insira uma linha horizontal e somente as duas linhas técnicas do modelo abaixo.

```markdown
<descrição da versão em uma única estrofe>

---

**Compatibility:** Minecraft 26.2 / [Fabric Loader 0.19.3+](https://fabricmc.net/use/installer/)
**Dependencies:** [Fabric API 0.155.0+26.2](https://modrinth.com/mod/fabric-api) / [Mod Menu 20.0.1+](https://modrinth.com/mod/modmenu)
```

## Validação

- Nunca abra o Minecraft ou qualquer instância para validar alterações.
- Nunca execute `runClient` ou outra tarefa que inicialize o jogo.
- Não use automação de interface para abrir ou controlar o Minecraft.
- Valide alterações somente por build, revisão estática do código, inspeção dos arquivos gerados e análise dos logs produzidos pelo teste manual do usuário.
- Quando uma alteração de interface ou gameplay precisar de confirmação dentro do jogo, informe o que deve ser observado e deixe o teste manual para o usuário.
- Continue projetando a interface para fullscreen, GUI Scale `2x`, identidade visual vanilla e ausência de cortes ou sobreposições, mas sem iniciar o jogo para conferir.

## Escopo atual

- Mod client-side focado em singleplayer e executado no servidor integrado.
- A configuração é acessada pelo Mod Menu.
- O mod adiciona e remove drops de monstros conforme marcadores configuráveis.
- Em servidores multiplayer externos, o servidor controla os drops; não prometa suporte multiplayer apenas com instalação no cliente.

## Padrões da primeira instalação

- Todas as opções principais devem iniciar ativas por padrão na primeira instalação.
- Todas as opções globais devem iniciar inativas por padrão na primeira instalação.
- Novas opções principais e globais devem seguir esses mesmos padrões, inclusive quando exigirem migração do formato de configuração.
- Configurações antigas podem ser migradas uma única vez quando houver mudança de versão do formato.
- Depois da migração, escolhas já salvas pelo jogador devem ser preservadas.

## Clareza e precisão do README

- Garanta que todas as opções e funcionalidades destinadas ao jogador estejam descritas de forma completa, clara e explicativa.
- Confira o comportamento real no código antes de editar a documentação e compare as descrições com constantes, listas, condições e valores implementados.
- Informe todos os valores fixos relevantes, incluindo quantidades, multiplicadores, porcentagens, distâncias, alcances, durações, intervalos, limites, chances e tempos.
- Essa precisão é especialmente importante quando o mod não permite que o jogador altere esses valores.
- Não use expressões vagas como “mais rápido”, “por algum tempo”, “itens compatíveis” ou nomes genéricos de categorias quando existir um valor, prazo, lista ou critério exato no código.
- Para cada opção, explique quando ela é ativada, o que afeta, o que permanece inalterado e quais são suas exceções, exclusões, condições de término e comportamentos especiais.
- Quando existir uma lista fechada de itens, blocos, entidades ou categorias, enumere seus integrantes ou defina precisamente a categoria usada.
- Informe os valores e estados padrão da primeira instalação e esclareça como configurações existentes são preservadas ou migradas.
- Não há problema se as descrições ficarem longas. Priorize clareza e precisão, pois o jogador precisa entender exatamente o comportamento oferecido.
- Ao adicionar ou corrigir uma opção, revise também as demais descrições do README e complete valores, limites, escopos ou exceções que estejam ausentes.
- Não invente comportamentos. Toda afirmação deve corresponder ao código atual.
