resultat <- read.table("denombrementCIP5112016.csv",sep="\t",header=F,quote="")
colnames(resultat) <- c("mol1","mol2","datemin","datemax","N")
resultat$datemin <- NULL
resultat$datemax <- NULL
sum(resultat$N)
nrow(resultat)
resultat <- resultat[order(-resultat$N),]

## chargement du thesaurus
library(IMthesaurusANSM)
df <- thesaurus72013$df_decompose
colnames(df)
df$mol1 <- gsub ("^[ ]+|[ ]+$","",df$mol1)
df$mol2 <- gsub ("^[ ]+|[ ]+$","",df$mol2)
df <- unique(df)
bool <- df$mol1 == df$mol2
sum(bool)
df <- subset (df, !bool)

df2 <- merge (df, resultat, by=c("mol1","mol2"))
df2 <- df2[order(-df2$N),]
sum(df2$N)
colnames(df2)
df3 <- subset (df2, select=c(mol1,mol2,prota2,prota1,niveau,N))
### filtrer pour retirer les interactions dépendantes du contexte

