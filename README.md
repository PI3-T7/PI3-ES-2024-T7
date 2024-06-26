# Projeto Locação de Armários

## Descrição do Projeto

### Motivação
> Na atualidade, turistas costumam levar seus pertences e equipamentos eletrônicos à praia,
shows, eventos como festas lotadas e muitas outras situações. Mas, quando decidem
aproveitar o momento e se desligar da preocupação, não conseguem, pois os itens pessoais
estão a todo momento precisando de cuidados. Deixar com alguém nem sempre é uma
opção.
Além do incômodo de carregar as bugigangas, há a preocupação com assaltos ou arrastões
como acontecem nas praias e, nessas situações, muita gente sai no prejuízo com o furto de
itens de valor.
Baseado neste cenário, o objetivo deste projeto integrador é construir uma solução de
locação de armários para que em locais de entretenimento, pessoas possam guardar seus
itens pessoais com segurança.

### Solução Proposta
> A proposta para resolver os problemas citados anteriormente é uma solução tecnológica
composta de: armário automatizado e um aplicativo Android que será usado pelo cliente
final (aquele que deseja guardar itens) e pelo gerente de uma unidade locadora de armários.

<!-- ## Tecnologias Utilizadas
Este projeto utiliza as seguintes tecnologias e linguagens: -->

## Colaboradores
Este projeto está sendo desenvolvido pelos integrantes do Time 7 da disciplina de Projeto Integrador 3 - Engenharia de Software (PUC-Campinas): Alex Insel, Marcos Miotto, Isabella Tressino e Lais Lemos.

## Orientações para instalação

### Para colaboradores:

1. **Fazer um Fork do repositório original**
   * Aperte no botão "Fork" no canto superior direito do repositório original.
   * Marque a opção "Copy the main branch only".
   * Aperte "Create Fork".

2. **Criar um diretório/projeto no Android Studio e clonar o repositório**
   * Crie um projeto novo no Android Studio.
   * Abra o terminal do Android Studio.
   * Dê o comando `git init`.
   * Vá ao repositório "forkado", que estará entre os repositórios da sua conta no GitHub, e copie o link HTTPS (Muita atenção aqui, é o HTTPS do fork, não use o do repositório original).
   * Dê o comando `git clone https` (no lugar de `https`, você deve colocar o link HTTPS que copiou).
   * Em seguida, dê o comando `git remote add upstream https` (aqui o `https` deverá ser do repositório original).
   * Por fim, dê o comando `git fetch upstream`.

3. **Criando uma branch nova**
   * Toda vez que algum colaborador for contribuir com uma nova funcionalidade para o código, ele deverá fazer isso por meio de uma branch.
   * Antes de qualquer coisa, dê o comando `git checkout -b de-um-nome-para-sua-branch`.
   * Exemplo: `git checkout -b Tela-Esqueci-Minha-Senha`.
   * Esse comando vai criar uma branch nova e entrar nela logo em seguida.
   * Use `git branch` para confirmar que está na branch nova, e não na main. Se ainda estiver na main, use `git checkout nome-da-branch`.
   * Assim que confirmar que está em uma branch nova, está liberado para contribuir com o código!
   * **IMPORTANTE:** Não altere o código que já estava ali anteriormente, somente adicione o seu, para evitar conflitos de merge. (A menos que seja uma branch específica para isso, ou seja, caso você ache que o código de outra pessoa tem um erro, converse com ela e peça para ela mesma mudar em uma branch específica "fix", que sofrerá um merge direto com a main - pois é uma correção de erro).

4. **Documentando o seu código**
   * Façam bastante comentários que expliquem o código durante o desenvolvimento.
   * Também façam muitos commits, sendo os mais descritivos possíveis sem escrever frases muito grandes. (Cada vez que for fazer um commit, deve usar `git add .` antes).

5. **Subindo o código para o repositório**
   * Alguns passos importantes a seguir antes de subir o código:</br></br>
     a) Tenha certeza que seu clone está atualizado com o repositório original. Para isso, você deve entrar no seu repositório "forkado" do GitHub e verificar se ele está sincronizado com o original, apertando no      botão "Sync Fork" e depois "Update branch".</br></br>
     b) Revise o seu código para evitar subir erros, e caso encontre algum, arrume-o e faça o commit que explica a mudança.</br></br>
     c) Entre no repositório original e crie uma branch nova, pelo GitHub mesmo (se não souber como, fale com sua gerente de configurações), com o mesmo nome da branch que criou para a nova funcionalidade.</br> 
     </br>
   * Após os passos acima, dê o comando `git push --set-upstream origin nome-da-sua-branch`.
   * Esse comando irá subir as mudanças locais para o seu repositório "forkado".
   * Vá até seu GitHub, entre no repositório forkado e aperte em "Compare and Pull request".
   * **Muita ATENÇÃO agora:** Verifique se está fazendo o pull request da sua branch do repositório clonado, para a branch de mesmo nome do repositório original, que criamos agora pouco.
   * Prontinho, sua gerente de configurações irá revisar o pull request para aceitá-lo, ou solicitar alguma mudança antes do merge, se necessário.
   * Se o Pull request estiver dando erro, verifique agora se a branch nova está sincronizada com o repositório original, apertando em sync fork e update branch.
   * Não aceitem os pull requests vocês mesmos, e não façam merge de nenhuma branch com a main no repositório original, elas serão unidas com a main por meio de Tags periódicas.
   * Assim que finalizarem o pull request, e se quiserem trabalhar em outra funcionalidade, criem uma branch nova pelo Android Studio.

