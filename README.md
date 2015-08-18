Plataforma SDNMon ...

Atualização 18_08_2015

1- Todas as tabelas do BD são limpas antes do SDNMon começar a povoar as tabelas.
2- Foi adicionado o sistema de junção de Threads. IP->Porta->Switch.
   2.1 -> A junção dos ips se dá quando todos os IPs mapeados por aquela porta    estão inativos (não passam nenhum tráfego).
   2.2 -> A junção das portas acontece quando todas as portas daquele switch estão inativas (não passam nenhum tráfego). 

3- Um fluxo que começar após quebra de sua porta responsável, irá ser alocado como granularidade IP. 
4- A sincronia está sendo feita através do modelo Leitor-Escritor.  

Próximos Passos (Fazer depois)
1 - Como houve muitas mudanças no código, é possível que possa ter variáveis ou operações redundantes. 
2 - ???

