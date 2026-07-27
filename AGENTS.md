# DROP CONTROL — Instruções do ambiente de desenvolvimento

Leia este arquivo antes de alterar, compilar ou testar o projeto.

## Ambiente

- Sistema: Windows com PowerShell.
- Minecraft: `26.2`.
- Fabric Loader: `0.19.3`.
- Fabric API: `0.155.0+26.2`.
- Mod Menu: `20.0.1`.
- Java: `25`.
- JDK usado no desenvolvimento:
  `C:\Users\marku\AppData\Local\Temp\smarttrade-jdk25\jdk-25.0.3+9`

## Build

Antes de executar qualquer operação potencialmente demorada, como build, varredura ampla ou outra tarefa pesada, informe em uma frase por que ela é necessária. Se não houver necessidade técnica, não execute a operação.

Execute o build com o JDK 25 configurado:

```powershell
$env:JAVA_HOME='C:\Users\marku\AppData\Local\Temp\smarttrade-jdk25\jdk-25.0.3+9'
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

1. Faça o deploy na instância abaixo:
   - `D:\MARKUS\GAMES\minecraft\instances\NEBULOSA6\minecraft\mods`
2. Resolva e valide que o destino corresponde exatamente a essa pasta autorizada.
3. Localize nessa pasta somente arquivos que correspondam a:
   `dropcontrol-*.jar`
4. Remova somente esses JARs antigos do DROP CONTROL.
5. Copie o novo JAR de `build\libs` para a pasta de mods.
6. Confirme que existe exatamente um `dropcontrol-*.jar` no destino e que ele corresponde à versão recém-compilada.

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

- Todos os marcadores de drop devem iniciar selecionados.
- Todas as opções globais devem iniciar desativadas.
- Configurações antigas podem ser migradas uma única vez quando houver mudança de versão do formato.
- Depois da migração, escolhas já salvas pelo jogador devem ser preservadas.
