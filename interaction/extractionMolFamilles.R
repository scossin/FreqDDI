rm(list=ls())
library(IMthesaurusANSM)
df <- thesaurus72013
## Familles : 
variables <- c("thesaurus2009","thesaurus2010", "thesaurus2011", "thesaurus032012", 
"thesaurus122012", "thesaurus72013","thesaurus2014","thesaurus012015",
"thesaurus062015")

### chargement de toutes les extractions
df <- NULL
i <- variables[1]
for (i in variables){
  temp <- get(i)
  df <- rbind(df,temp$mol_famille)
  df <- unique(df)
}
df <- subset (df, select=c("famille"))
df <- unique(df)
write.table(df,"famillesThesauri.csv", sep="\t", col.names=T, row.names=F)

##############Molecules : 
### chargement de toutes les extractions
df <- NULL
i <- variables[1]
for (i in variables){
  temp <- get(i)$df_decompose
  temp$origine <- NULL
  temp <- unique(temp)
  temp$thesaurus <- i
  df <- rbind (df, temp)
}

## merge avec la table : 
df$mol1 <- gsub ("^[ ]+|[ ]+$","",df$mol1)
df$mol2 <- gsub ("^[ ]+|[ ]+$","",df$mol2)
df <- unique(df)

### molecule du thesaurus  : 
moleculesthesaurus <- unique(c(df$mol1, df$mol2))
length(moleculesthesaurus)
writeLines(moleculesthesaurus, "moleculesthesaurus.txt")
