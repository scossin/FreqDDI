rm(list=ls())
#### le fichier CSV fait 112 mega
## pour le charger plus vite et pour le partage, on en fait un fichier rdata
# resultat <- read.table("denombrementCIP12112016.csv",sep="\t",header=F,quote="")
# # resultat2 <- resultat
# colnames(resultat) <- c("mol1","mol2","vide","Age","num","Nmedocs")
# ## une tabulation en trop dans le programme
# resultat$vide <- NULL
# str(resultat)
# save(resultat, file="resultat.rdata")
load("resultat.rdata")
length(unique(resultat$num)) ## 1 115 284 ordonnances contenant au moins une interaction
resultat$couple <- paste(resultat$mol1, resultat$mol2, sep=";")
couples <- unique(resultat$couple)
length(unique(resultat$couple)) ### 14 279 couples uniques 
length(unique(resultat$num)) ### 1 115 284 ordonannces différentes avec une interaction

### ajouter le niveau de l'interaction 
## chargement du thesaurus (cf installation sur github)
# devtools::install_github("scossin/IMthesaurusANSM")
library(IMthesaurusANSM)
df <- thesaurus72013$df_decompose
df$mol1 <- gsub ("^[ ]+|[ ]+$","",df$mol1)
df$mol2 <- gsub ("^[ ]+|[ ]+$","",df$mol2)
df <- unique(df)
df$couple <- paste(df$mol1, df$mol2, sep=";")
bool <- df$mol1 == df$mol2
sum(bool)
df <- subset (df, !bool)
colnames(df)
length(unique(df$couple))
## on retire les couples non présents dans les résultats
bool <- df$couple %in% couples
df <- subset (df, bool)
bool <- couples %in% df$couple
all(bool) ## tous les couples dénombrés sont bien connus

## on retire les origines conditionnelles : 
# protagonistes avec conditions : 
familles <- read.table("../interaction/famillesThesauri.csv",sep="\t",header=T,quote="")
bool <-is.na(familles$contexte)
familles$contexte
familles <- subset (familles, !bool)
famillescontexte <- familles$famille
length(famillescontexte) ### 7 familles dépendantes du contexte

bool <- df$prota1 %in% famillescontexte | df$prota2 %in% famillescontexte
sum(bool)
contexte <- subset (df, bool)
df2 <- subset (df, !bool)

## on retire les origines conditionnelles du dénombrement
bool <- resultat$couple %in% contexte$couple
contexte <- subset (resultat, bool)
nrow(contexte) ## 148 896 alertes dépendantes du contexte 

## ajouter le niveau au dénombrement (dataframe resultat)
# si plusieurs niveaux, on choisit le niveau le plus haut
tab <- tapply(df2$niveau, df2$couple,max)
tab <- data.frame (couple=names(tab),niveau=as.numeric(tab))
df3 <- merge (df2, tab, by=c("couple","niveau"))
couplesniveau <- subset (df3, select=c(couple,niveau))
couplesniveau <- unique(couplesniveau)
length(unique(couplesniveau$couple))
length(unique(resultat$couple))
bool <- resultat$couple %in% couplesniveau$couple
sum(!bool)
voir <- subset (resultat,!bool) ### ceux qui sont exclus car contexte dépend mais pas d'interaction hors contexte
voir <- merge (df, voir, by="couple")
table(voir$niveau)
voir2 <- subset (voir,niveau==4)

### ajout du niveau (pas faire de merge car ça prend du temps)
resultat <- subset (resultat, bool)
length(unique(resultat$num))
1114413 / 3561144
resultat$couple <- as.factor(resultat$couple)
resultat <- resultat[order(resultat$couple),]
tab <- table(resultat$couple)
tab <- data.frame(couple=names(tab), frequence=as.numeric(tab))
tab <- merge (tab, couplesniveau, by="couple")
tab <- tab[order(tab$couple),]
niveaux <- rep(tab$niveau, tab$frequence)
resultat$niveaux <- niveaux
resultat$niveaux <- as.factor(resultat$niveaux)
levels(resultat$niveaux) <- c("PC","PE","AD","CI")
table(resultat$niveaux)
## cumsum
tab<-tab[order(-tab$frequence),]
cumul <- cumsum(tab$frequence)
min(which(cumul >sum(tab$frequence) / 2))

###################################### graphiques sur les niveaux #####################################"
library(tikzDevice)
tikz("/home/cossin/DP/These/Tex/these_seb/plot/alertes.tex",width=7, height=6)
nrow(resultat)
tab <- table(resultat$niveau)
bplt <- barplot(tab, ylim=c(0, max(tab) + 0.2*max(tab)), ylab="Nombre d'alertes générées")
tab_percent <- round (100*tab/sum(tab, na.rm=T),1)
total <-  format(as.numeric(tab), big.mark = " ")
texte <- paste (total, " (",tab_percent, "\\%)", sep="")
text (x= bplt, y=tab + 0.1*max(tab), labels=as.character(texte))
dev.off()

##################### couples les plus fréquents #########################
#### tous :
table(resultat$niveau)
str(resultat)
tab <- table(resultat$couple)
tab <- data.frame(couple=names(tab), frequence=as.numeric(tab))
tab <- tab[order(-tab$frequence),]
tab <- tab[1:10,]
voir <- merge (df3, tab, by="couple")
voir <- subset (voir,select=c(mol1, mol2, niveau,frequence))
voir <- voir[order(-voir$frequence),]
voir[1:10,]

#### les contre-indications
temp <- subset (resultat, niveaux == "CI")
tab <- table(temp$couple)
tab <- data.frame(couple=names(tab), frequence=as.numeric(tab))
tab <- tab[order(-tab$frequence),]
voir <- merge (df3, temp, by="couple") ### toujours la même famille
voir$coupleprota <- paste(voir$prota1, voir$prota2, sep=";")
sort(table(voir$coupleprota)) 
16069 / nrow(temp) #### 94% concernent 
all(voir$couple %in% tab$couple)
table(df3$niveau)
tab <- tab[1:10,]
voir <- merge (df3, tab, by="couple") ### toujours la même famille
voir <- subset (voir,select=c(mol1, mol2, niveau,frequence))
voir <- voir[order(-voir$frequence),]
voir$couple <- paste(voir$mol1, voir$mol2, sep=";")
voir[1:10,]
library(xtable)
voir$mol1 <- tolower(voir$mol1)
voir$mol2 <- tolower(voir$mol2)
voir$niveau <- NULL
voir$niveau <- "CI"
voir <- subset (voir, select=c("niveau","mol1","mol2","frequence"))
sum(voir$frequence)
voir$frequence <- as.character(voir$frequence)
print(xtable(voir), include.rownames = F)

################################ graphique : âge ###########################
### ce fichier provient du dénombrement avec le programme JAVA
df_age <- read.table("liste_age.csv",sep="\t",header=F)
## certains ages sont impossibles, on retire après 100 ans pour le graphique
colnames(df_age) <- c("age","N")
df_age <- subset (df_age, !age > 100)
tikz("/home/cossin/DP/These/Tex/these_seb/plot/age.tex",width=7, height=6)
bplt <- barplot(df_age$N,  ylim=c(0, max(df_age$N) + 0.2*max(df_age$N)),
                ylab="Nombre de délivrances",xlab="âge", xlim=c(0,130))
axis (side=1, at=bplt, labels=as.character(df_age$age))
dev.off()

################################ graphique : nombre de médicaments à passage systémique sur l'ordonnance ###############
denominateur <- read.table("liste_ordo.csv",sep="\t",header=F)
colnames(denominateur) <- c("Nmedocs","frequence")
library(tikzDevice)
taille_hauteur_largeur <- 3
tikz('/home/cossin/DP/These/Tex/these_seb/plot/Nmedocsordo.tex',width=7, 6)
par(mar=c(4,6,4,4))
total <- sum(denominateur$frequence)
total <-  format(total, big.mark = " ")
bplt <- barplot(denominateur$frequence, xlim=c(0,max(denominateur$frequence) + 0.2*max(denominateur$frequence)), las=1, horiz=T,
                ,xlab=paste("Nombre de délivrances. Total = ", total,sep=""),
                ylab="Nombre de médicaments avec un risque potentiel d'interaction 
                sur l'ordonnance délivrée")
axis(side=2, at=bplt, labels=as.character(denominateur$Nmedocs), las=1)
nombre <- format(denominateur$frequence, big.mark = " ")
# pourcentage <- signif(100*df_ordo$N / sum(df_ordo$N),1)
# nombre <- paste (nombre, " (", pourcentage,"\\%)",sep="")
text(x=denominateur$frequence + 0.05 * denominateur$frequence + 150000, 
     y = bplt, labels=as.character(nombre))
dev.off()
par(mar=c(4,4,4,4))


########################################### graphique : % d'interaction selon le nombre de médicaments ##################################
############ je fais ici les pourcentages pour chacun (CI,AD,...)
## denominateur :
denominateur <- read.table("liste_ordo.csv",sep="\t",header=F)
colnames(denominateur) <- c("Nmedocs","frequence")
sum(denominateur$frequence) ### 7 015 571 ordonnances
denominateur <- subset (denominateur, Nmedocs > 1)
sum(denominateur$frequence) ### 3 501 702 ordonnances avec au moins 2 médicaments à passage systémique
length(unique(resultat$num)) ### 1 058 660 ordonnances avec au moins une interaction 
length(unique(resultat$num)) / sum(denominateur$frequence) ## 30.2 %
## copie de travail 
denominateur2 <- denominateur
  
niveaux <- c("CI","AD","PE","PC", "all")
for (i in niveaux){
  if (i == "all"){
    temp <- unique(subset (resultat, select=c(num,Nmedocs)))
    tab <- table(temp$Nmedocs)
    tab <- data.frame(Nmedocs=names(tab), all=as.numeric(tab))
    denominateur2 <- merge (denominateur2, tab, by="Nmedocs",all.x=T)
  } else {
    temp <- unique(subset (resultat, niveaux == i,select=c(num,Nmedocs)))
    tab <- table(temp$Nmedocs)
    tab <- data.frame(Nmedocs=names(tab), nombre=as.numeric(tab))
    colnames(tab) <- c("Nmedocs",i)
    denominateur2 <- merge (denominateur2, tab, by="Nmedocs",all.x=T)
  }
cat(i, " fait \n")
}
bool <- is.na(denominateur2)
denominateur2[bool] <- 0
## je merge à partir de 17
bool <- denominateur2$Nmedocs > 17
temp <- subset (denominateur2, bool)
temp <- data.frame(t(data.frame(colSums(temp))))
temp$Nmedocs <- 18
denominateur2 <- subset (denominateur2, !bool)
denominateur2 <- rbind (denominateur2, temp)
denominateur2$Nmedocs <- gsub ("18",">17",denominateur2$Nmedocs)
## sortie graphique

## il faut ré-organiser la dataframe
colnames(denominateur2)
i <- "all"
denominateur3 <- NULL
for (i in niveaux){
  temp <- subset (denominateur2, select=c("Nmedocs","frequence",i))
  colnames (temp) <- c("Nmedocs","frequence","niveau")
  temp$pourcentage <- round(100*temp$niveau / temp$frequence,1)
  temp$niveau <- i
  temp$id <- 1:nrow(temp)
  denominateur3 <- rbind (denominateur3, temp)
}
denominateur3 <- denominateur3[with(denominateur3,order(id,niveau)),]
denominateur3$niveau <- as.factor(denominateur3$niveau)
levels(denominateur3$niveau)[2] <- c("Au moins 1")
denominateur3$niveau <- factor(denominateur3$niveau, levels=c("Au moins 1","PC","PE","AD","CI"))
couleurs <- c("#72A8A5","#60DB6B","#419648","orange","red")
key= list(x=0.05, y=0.9, 
          text=list(levels(denominateur3$niveau)),
          rectangles=list(col=couleurs),
          x.intersp=10)
## graphique
library(tikzDevice)
taille_hauteur_largeur <- 4
tikz('/home/cossin/DP/These/Tex/these_seb/plot/pourcentage_interaction.tex',width=taille_hauteur_largeur*1.8, height=taille_hauteur_largeur*1.5)
library(lattice)
barchart(pourcentage~id,data=denominateur3,groups=denominateur3$niveau, col=couleurs, ylim=c(0,100),horizontal=FALSE,
         ylab="Pourcentage de délivrances avec un risque avéré d'interaction
         d'après le thesaurus de l'ANSM",
         xlab="Nombre de médicaments délivrés avec un risque potentiel d'interaction",
         key=key,
         scales=list(y=list(cex=1, at = seq(0,100,10), labels=as.character(seq(0,100,10))),
                     x=list(cex=1, at=1:18, labels=as.character(unique(denominateur3$Nmedocs)))))
dev.off()



# ##### graphiques sur le nombre d'interactions dans le thesaurus 2016 :
# rm(list=ls())
# library(IMthesaurusANSM)
# tab_interaction_2016 <- thesaurus012016$df
# tab_interaction_2016$niveau <- ifelse (!is.na(tab_interaction_2016$PC), 1,
#                                        ifelse (!is.na(tab_interaction_2016$PE), 2,
#                                                ifelse (!is.na(tab_interaction_2016$AD), 3,
#                                                        4)))
# table(tab_interaction_2016$niveau)
# tab_interaction_2016$niveau <- as.factor(tab_interaction_2016$niveau)
# levels(tab_interaction_2016$niveau) <- c("PC","PE","AD","CI")
# ##################################  niveau d'interaction  par protagoniste ####################
# require(tikzDevice)
# tikz("/home/cossin/DP/These/Tex/these_seb/plot/niveau.tex",width=6, height=3.7)
# tab <- table(tab_interaction_2016$niveau)
# bplt <- barplot(tab, ylim=c(0, max(tab) + 0.2*max(tab)), ylab="Nombre de couples protagoniste 'a'-protagoniste'b'",
#                 xlab="niveau de contrainte")
# tab_percent <- round (100*tab/sum(tab, na.rm=T),1)
# texte <- paste (tab, " (",tab_percent, "\\%)", sep="")
# text (x= bplt, y=tab + 0.1*max(tab), labels=as.character(texte))
# dev.off()
# 
# ################################## Niveau d'interaction par molécules
# rm(list=ls())
# library(IMthesaurusANSM)
# df <- thesaurus72013$df_decompose
# df$mol1 <- gsub ("^[ ]+|[ ]+$","",df$mol1)
# df$mol2 <- gsub ("^[ ]+|[ ]+$","",df$mol2)
# df <- unique(df)
# df$couple <- paste(df$mol1, df$mol2, sep=";")
# bool <- df$mol1 == df$mol2
# sum(bool)
# df <- subset (df, !bool)
# colnames(df)
# length(unique(df$couple))
# ## ajouter le niveau au dénombrement (dataframe resultat)
# # si plusieurs niveaux, on choisit le niveau le plus haut
# tab <- tapply(df$niveau, df$couple,max)
# tab <- data.frame (couple=names(tab),niveau=as.numeric(tab))
# df2 <- merge (df, tab, by=c("couple","niveau"))
# df3 <- subset (df2, select=c("couple","niveau"))
# df3 <- unique(df3)
# nrow(df3) - nrow(df)
# 10774 / 52870
# require(tikzDevice)
# tikz("/home/cossin/DP/These/Tex/these_seb/plot/niveau_decompose.tex",width=6, height=3.5)
# df3$niveau <- as.factor(df3$niveau)
# levels(df3$niveau) <- c("PC","PE","AD","CI")
# tab <- table(df3$niveau)
# bplt <- barplot(tab, ylim=c(0, max(tab) + 0.2*max(tab)), ylab="Nombre de couples molécule1-molécule2",
#                 xlab="niveau de contrainte")
# tab_percent <- round (100*tab/sum(tab, na.rm=T),1)
# texte <- paste (tab, " (",tab_percent, "\\%)", sep="")
# text (x= bplt, y=tab + 0.1*max(tab), labels=as.character(texte))
# dev.off()
