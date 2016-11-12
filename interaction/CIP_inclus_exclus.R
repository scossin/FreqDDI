################# L'objectif de ce programme est de créer les fichiers nécessaires au programme JAVA de dénombrement des interactions : 
####### Liste des codes CIP exclus (car voie locale) et inclus (à risque d'interaction)
####### Correspondence dci -> codesCIP
####### Table des interactions 

#### Etape 1
####################### Liste des codes CIP exclus (car voie locale) et inclus (à risque d'interaction) ###########
rm(list=ls())
## chargement : 
load("RepertoireMedicament/ANSM.rdata")
ANSM_CIS <- ANSM$ANSM_CIS
ANSM_CIP <- ANSM$ANSM_CIP
ANSM_COMPO <- ANSM$ANSM_COMPO

######## liste des médicaments exclus localement
### voies :
noms <- names(table(ANSM_CIS$voie))
noms2<- lapply(noms, function(x){
  unlist(strsplit(x,";"))
})
noms3 <- unique(unlist(noms2))
names(table(ANSM_CIS$voie))
### d'après les informations du thesaurus des interactions
# collyre : voie locale
# nasale : voie locale
voies_exclus <- c("auriculaire","ophtalmique","cutanée","nasale")
voies_exclus_regex <- paste (voies_exclus, collapse="|")
bool <- grepl(voies_exclus_regex, ANSM_CIS$voie) & !grepl("sous-cutanée",ANSM_CIS$voie)
sum(bool)
voir <- subset (ANSM_CIS, bool)
CIP_exclus_local <- subset (ANSM_CIP, CIS %in% voir$CIS, select=c(CIP7))
CIP_exclus_local <- unique(CIP_exclus_local)
###### liste des médicaments exclus car homéopathie
## rechercher par enregistrement
bool <- ANSM_CIS$type == "Enreg homéo (Proc. Nat.)"
voir <- subset (ANSM_CIS, bool)
voir <- merge (ANSM_CIP, voir, by="CIS")
voir <- merge (ANSM_COMPO, voir, by="CIS")
enreg_CIS <- unique(voir$CIS)
### recherche via degré de dilution
bool <- grepl("CH[^A-Z]",ANSM_COMPO$dosage) | grepl("DH[^A-Z]",ANSM_COMPO$dosage) | 
  grepl("TM[^A-Z]",ANSM_COMPO$dosage)
sum(bool)
voir2 <- subset (ANSM_COMPO, bool)
voir2 <- merge (ANSM_CIS, voir2, by="CIS")
table(voir2$type)
dilution_CIS <- unique(voir2$CIS)
### recherche le terme dans la substance
bool <- grepl("homeopat|homéopath",ANSM_COMPO$substance,ignore.case = T)
sum(bool)
voir3 <- subset (ANSM_COMPO, bool)
voir3 <- merge (ANSM_CIS, voir3, by="CIS")
table(voir3$type)
homeopat_CIS <- unique(voir3$CIS)
all_CIS <- c(dilution_CIS, homeopat_CIS, enreg_CIS)
all_CIS <- unique(all_CIS)
length(unique(all_CIS)) ## 933 

CIP_exclus_homeo <- subset (ANSM_CIP, CIS %in% all_CIS, select=c(CIP7))
CIP_exclus_homeo <- unique(CIP_exclus_homeo)

CIP_exclus <- unique(c(CIP_exclus_homeo$CIP7,CIP_exclus_local$CIP7))

#### CIP_exclus : la liste des CIP7 exclus
### mais il existe des exceptions

### 1) Betabloquant : 
molfamille <- read.table("MoleculesfamillesThesauri.csv",sep="\t",header=T)
bool <- grepl("beta-bloquant",molfamille$famille,ignore.case = T)
sum(bool)
voir <- subset (molfamille, bool)
table(as.character(voir$famille))
BB <- unique(tolower(voir$molecule))
## ajout pilocarpine
BB <- c(BB, "pilocarpine")

## besoin des alignements vers le thesaurus : 
alignement <- read.table("RepertoireMedicament/RelationThesaurusRepertoireFinal28102016.csv",sep="\t",header=T)
alignement$dci <- tolower(alignement$dci)
BBalignement <- subset (alignement, dci %in% BB)

BB_COMPO <- subset (ANSM_COMPO, code %in% BBalignement$code,select=c(CIS))
BB_CIS <- subset (ANSM_CIS, CIS %in% BB_COMPO$CIS & voie == "ophtalmique",select=c(CIS))
BB_CIP <- subset (ANSM_CIP, CIS %in% BB_CIS$CIS,select=c(CIP7))
BB_CIP <- unique(BB_CIP$CIP7)

### 2) voie nasale 
voie_n <- c("desmopressine","dihydroergotamine","fentanyl")
bool <- alignement$dci %in% voie_n
voie_nalignement <- subset (alignement, bool)
voie_n_COMPO <- subset (ANSM_COMPO, code %in% voie_nalignement$code,select=c(CIS))
voie_n_CIS <- subset (ANSM_CIS, CIS %in% voie_n_COMPO$CIS & voie == "nasale",select=c(CIS))
voie_n_CIP <- subset (ANSM_CIP, CIS %in% voie_n_CIS$CIS, select=c(CIP7))
voie_n_CIP <- unique(voie_n_CIP$CIP7)

## 3) toutes voies
voie_t <- c("miconazole","econazole")
bool <- alignement$dci %in% voie_t
voie_talignement <- subset (alignement, bool)
voie_t_COMPO <- subset (ANSM_COMPO, code %in% voie_talignement$code,select=c(CIS))
voie_t_CIS <- subset (ANSM_CIS, CIS %in% voie_t_COMPO$CIS,select=c(CIS))
voie_t_CIP <- subset (ANSM_CIP, CIS %in% voie_t_CIS$CIS, select=c(CIP7))
voie_t_CIP <- unique(voie_t_CIP$CIP7)

## resume : 
voie_exception <- c (BB_CIP, voie_n_CIP, voie_t_CIP)
voie_exception <- unique(voie_exception)
bool <- CIP_exclus %in% voie_exception
any(bool)
sum(bool) ## 403 CIP7 non exclus car exceptions
CIP_exclus <- CIP_exclus[!bool]
length(CIP_exclus) ## 4310 CIP7 exclus

######### liste des CIP présents dans les données
### pour obtenir cette liste, faire tourner le programme JAVA denombremer.java
CIPpresent <- read.table("../output/liste_CIP.csv",sep="\t")
colnames(CIPpresent) <- c("CIP7","N")
bool <- is.na(CIPpresent$CIP7)
CIPpresent$CIP7[bool] <- ""
CIPpresent$categorie <- NA
boolhomeo <- CIPpresent$CIP7 %in% CIP_exclus_homeo$CIP7
boollocal <- CIPpresent$CIP7 %in% CIP_exclus_local$CIP7
boolinconnus <- !CIPpresent$CIP7 %in% ANSM_CIP$CIP7
boolincorrect <- nchar(CIPpresent$CIP7) != 7
boolconnu <- CIPpresent$CIP7 %in% ANSM_CIP$CIP7
boolexception <- CIPpresent$CIP7 %in% voie_exception
CIPpresent$categorie <- ifelse (boolincorrect, "incorrect",
                                ifelse (boolinconnus,"inconnu",
                                        ifelse(boolconnu, "connu",CIPpresent$categorie)))
any(is.na(CIPpresent$categorie)) ### permet de vérifier que tout code CIP7 a une catégorie
table(CIPpresent$categorie)
### homéopathie et locale
CIPpresent$categorie <- ifelse (boolhomeo,"homeo",
                                ifelse(boollocal,"local",CIPpresent$categorie))
table(CIPpresent$categorie)
CIPpresent$categorie <- ifelse (boolexception,"exception",CIPpresent$categorie)
table(CIPpresent$categorie)

#### Nombre de spécialités thérapeutiques par catégorie
tapply(CIPpresent$N, CIPpresent$categorie, sum)
table(CIPpresent$categorie)
## CIP à risque : 
bool <- CIPpresent$categorie == "connu" | CIPpresent$categorie == "exception"
CIP7inclus <- CIPpresent$CIP7[bool]
length(CIP7inclus)
length(unique(CIP7inclus)) ### 12 286 médicaments CIP7 
############ seulement les codes CIP de cette liste seront indexés
#### ce sont des codes CIP : 
## présents dans les données 
## qui sont à risque d'interaction
## pour chaque ordonnance, le programme JAVA regardera dans cette liste : si CIP présent alors il indexe, sinon il ne compte pas
writeLines(CIP7inclus,"5112016/CIP7inclus.txt")






## Etape 2
######################################  Correspondence dci -> codesCIP #####################################
##### j'enlève les CIP exclus des alignementCIPs dciCIP (réalisés par le programme R dans répertoire du médicament)
## alignementCIP dci - CIP
alignementCIP <- read.table("RepertoireMedicament/dciCIP7.csv",sep="\t",header=F)
colnames(alignementCIP) <- c("dci","CIP7")
bool <- CIP7inclus %in% alignementCIP$CIP7
CIP7inclussans <- CIP7inclus[!bool]
voir <- subset (CIPpresent, CIP7 %in% CIP7inclussans)
voir <- merge (ANSM_CIP, voir, by="CIP7")
voir <- merge (voir, ANSM_COMPO, by="CIS")
voir<-voir[order(-voir$N),] ### principaux : virus de la grippe inactivé, PHLOROGLUCINOL
sum(bool) ## parmi les 12 286 médicaments dans nos données, 10 032 (sans les médicaments par voie ...)
  ## 11 624 concernés avec les voies : orale, vaginale, sels de fer par voie injectable
bool <- alignementCIP$CIP7 %in% CIP7inclus
alignementCIP <- subset (alignementCIP, bool)
tab <- tapply(alignementCIP$CIP7, alignementCIP$dci,function(x){
  paste (x,collapse=";") 
})
tab <- data.frame(mol1 = names(tab), CIP7=as.character(tab))
bool <- is.na(tab$CIP7)
sum(bool) ## 331 dci sans codes CIP7 dans les données
tab <- subset (tab, !bool)
write.table(tab, "5112016/moleculesCIP5112016.csv",sep="\t",col.names=F, row.names=F,quote=F)



########## Etape 3
##############################################   Liste des interactions    #################################
# library(devtools)
# devtools::install_github("scossin/IMthesaurusANSM")
library(IMthesaurusANSM)
### on prend le thesaurus de 2013 car les données sont dans cette période
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
### on garde seulement les molécules dont un code CIP est présent dans les données
bool <- interaction$mol1 %in% tab$mol1 & interaction$mol2 %in% tab$mol1
sum(bool)
interaction <- subset (interaction, bool)
write.table(interaction, "5112016/interactions5112016.csv",sep="\t",col.names=F, row.names=F,quote = F)

# ajout <- c("MEDICAMENTS UTILISES PAR VOIE VAGINALE","MEDICAMENTS ADMINISTRES PAR VOIE ORALE",
#   "SELS DE FER PAR VOIE INJECTABLE")
# bool <- interaction$mol1 %in% ajout | interaction$mol2 %in% ajout
# any(bool)
# interactionajout <- subset (interaction, bool)
# bool <- interactionajout$mol1 %in% tab$mol1 & interactionajout$mol2 %in% tab$mol1
# sum(bool)
# interactionajout <- subset (interactionajout, bool)
# write.table(interactionajout, "5112016/interactions5112016.csv",sep="\t",col.names=F, row.names=F,quote = F)


