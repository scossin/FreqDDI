rm(list=ls())
# library(devtools)
# devtools::install_github("scossin/IMthesaurusANSM")
library(IMthesaurusANSM)
df <- thesaurus72013$df_decompose
colnames(df)
df$mol1 <- gsub ("^[ ]+|[ ]+$","",df$mol1)
df$mol2 <- gsub ("^[ ]+|[ ]+$","",df$mol2)
df <- unique(df)

## les doublons ont déjà été retirés

## chaque thésaurus : date de la parution jusqu'à date - 1 de la parution de la mise à jour
df$date_min <- as.Date("01/01/2013",format="%d/%m/%Y")
df$date_max <- as.Date("31/12/2013",format="%d/%m/%Y")
## on retire sur mol1 et moL2 correspondent à la même molécule : 
bool <- df$mol1 == df$mol2
sum(bool)
df <- subset (df, !bool)
interaction <- subset (df, select=c(mol1, mol2, date_min,date_max))
interaction <- unique(interaction)
## liste des interactions dans le format demandé par le programme :
interaction$date_min <- format(interaction$date_min, format="%d/%m/%Y")
interaction$date_max <- format(interaction$date_max, format="%d/%m/%Y")
write.table(interaction, "5112016/interactions5112016.csv",sep="\t",col.names=F, row.names=F)
