# Projeto Locação de Armários
Este repositório tem a finalidade de versionar o componente curricular do terceiro semestre de Engenharia de Software da PUC Campinas: Projeto Integrador 3.
## Time 7
Este projeto está sendo desenvolvido pelos integrantes do Time 7 da disciplina de PI3: Alex Insel, Marcos Miotto, Isabella Tressino e Lais Lemos.
## Motivação do Projeto
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
## Solução Proposta
> A proposta para resolver os problemas citados anteriormente é uma solução tecnológica
composta de: armário automatizado e um aplicativo Android que será usado pelo cliente
final (aquele que deseja guardar itens) e pelo gerente de uma unidade locadora de armários.
## Orientações para instalação
### Para colaboradores:
1. Fazer um Fork do repositório original<br/>
* Apertar no botão "Fork" no canto superior direito do repositório original
* Marcar a opção "Copy the main branch only"
* Apertar "Create Fork"<br/>
2. Criar um diretório/projeto no Android Studio e clonar o repositório<br/>
* Crie um projeto novo no android studio
* Abra o terminal do android studio
* Dê o comando git init
* Vá ao repositório "forkado", que estará entre os repositórios da sua conta no GitHub, e copie o link HTTPS (Muita atenção aqui, é o HTTPS do fork, não use o do repositório original)
* Dê o comando "git clone https" (no lugar de https, você deve colocar o link HTTPS que copiou)
* Em seguida, dê o comando "git remote add origin https" (o mesmo link copiado anteriormente deverá ser colocado aqui)
* Por fim, dê o comando "git fetch upstream"<br/>
3. Criando uma branch nova
* Toda vez que algum colaborador for contribuir com uma nova funcionalidade para o código, ele deverá fazer isso por meio de uma branch.
* Antes de qualquer coisa, dê o comando "git checkout -b de-um-nome-para-sua-branch"
* Exemplo: git checkout -b Tela-Esqueci-Minha-Senha
* Esse comando vai criar uma branch nova e entrar nela logo em seguida.
* Use "git branch" para confirmar que está na branch nova, e não na main. Se ainda estiver na main, use "git checkout nome-da-branch"
* Assim que confirmar que está em uma branch nova, está liberado para contribuir com o código!
* IMPORTANTE: Não altere o código que já estava ali anteriormente, somente adicione o seu, para evitar conlfitos de merge. (A menos que seja uma branch específica para isso, ou seja, caso você ache que o código de outra pessoa tem um erro, converse com ela e peça para ela mesma mudar em uma branch específica "fix", que sofrerá um merge direto com a main - pois é uma correção de erro).
4. Documentando o seu código
* Façam bastante comentários que expliquem o código durante o desenvolvimento.
* Também façam muitos commits, sendo o mais descritivos possíveis sem escrever frases muito grandes. (Cada vez que for fazer um commit, deve usar "git add ." antes)
5. Subindo o código para o respositório
* Alguns passos importantes a seguir antes de subir o código:<br/>
a) Tenha certeza que seu clone está atualizado com o repositório original, para isso, você deve entrar no seu repositório "forkado" do GitHub e verificar se ele está sincronizado com o original apertando no botão "Sync Fork" na branch main. Depois, entre na branch criada e aperte o botão "Sync Fork" também. Após isso, dê o comando "git fetch upstream" no terminal do seu projeto no Android Studio. Se não 
se lembrar de fazer isso, fale com sua gerente de configurações, pois há uma forma de sincronizar após o push.<br/>
b) Revise o seu código para evitar subir erros, e caso encontre algum, arrume-o e faça o commit que explica a mudança.<br/>
c) Entre no repositório original e crie uma branch nova, pelo GitHub mesmo (se não souber como, fale com sua gerente de configurações), com o mesmo nome da branch que criou para a nova funcionalidade.<br/>
* Após os passos acima, dê o comando "git push --set-upstream origin nome-da-sua-branch".
* Esse comando irá subir as mudanças locais para o seu repositório "forkado".
* Vá até seu GitHub, entre no repositório forkado e aperte em "Compare and Pull request".
* Muita ATENÇÃO agora: Verifique se está fazendo o pull request da sua branch do repositório clonado, para a branch de mesmo nome do repositório original, que criamos agora pouco.
* Prontinho, sua gerente de configurações irá revisar o pull request para aceitá-lo, ou solicitar alguma mudança antes do merge, se necessário.
* Não aceitem os pull requests vocês mesmos, e não façam merge de nenhuma branch com a main no repositório original, elas serão unidas com a main por meio de Tags periódicas.
* Assim que finalizarem o pull request, e se quiserem trabalhar em outra funcionalidade, criem uma branch nova pelo android studio.
6. Qualquer dúvida, sintam-se a vontade para fazer perguntas :)
