resultat <- read.table("denombrementCIP5112016.csv",sep="\t",header=F,quote="")
colnames(resultat) <- c("mol1","mol2","datemin","datemax","N")
resultat$datemin <- NULL
resultat$datemax <- NULL
sum(resultat$N) ## 2 648 534 alertes
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
sum(df2$N) ## 3 495 968 total alertes
colnames(df2)
df3 <- subset (df2, select=c(mol1,mol2,prota2,prota1,niveau,N))
### filtrer pour retirer les interactions dépendantes du contexte

voir <- subset (df3, mol1=="AMIODARONE"&mol2=="ESCITALOPRAM")
voir <- subset (df3, mol1=="AMIODARONE"&mol2=="DOMPERIDONE")
voir <- subset (df3, mol1=="ESCITALOPRAM"&mol2=="SOTALOL")
voir <- subset (df3, mol1=="ESCITALOPRAM"&mol2=="HALOFANTRINE")
voir <- subset (df3, mol1=="DICLOFENAC"&mol2=="FUROSEMIDE")

bool <- grepl("POINTES",df2$prota1,ignore.case = T)
voir <- subset (df2, bool)
unique(voir$prota2)
unique(voir$prota1)

voir <- subset (df2, prota1 == "ANTIPARASITAIRES SUSCEPTIBLES DE DONNER DES TORSADES DE POINTES"
                & prota2 == "MEDICAMENTS SUSCEPTIBLES DE DONNER DES TORSADES DE POINTES")
familles <- thesaurus72013$mol_famille
voir <- subset (familles,famille=="ANTIPARASITAIRES SUSCEPTIBLES DE DONNER DES TORSADES DE POINTES")
