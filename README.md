Plataforma SDNMon ...

Atualização 18_08_2015

* Todas as tabelas do BD são limpas antes do SDNMon começar a povoar as tabelas.
* Foi adicionado o sistema de junção de Threads. IP->Porta->Switch.   
     A junção dos ips se dá quando todos os IPs mapeados por aquela porta estão inativos (não passam nenhum tráfego).   
     A junção das portas acontece quando todas as portas daquele switch estão inativas (não passam nenhum tráfego). 

* Um fluxo que começar após quebra de sua porta responsável, irá ser alocado como granularidade IP. 
* A sincronia está sendo feita através do modelo Leitor-Escritor.  

Próximos Passos (Fazer depois)

* Como houve muitas mudanças no código, é possível que possa ter variáveis ou operações redundantes. 
* ???