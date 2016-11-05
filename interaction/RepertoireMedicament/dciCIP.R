## Date de mise à jour 03/10/2016
### téléchargement du fichier sur le site de l'ANSM : 
# download.file(url="http://agence-prd.ansm.sante.fr/php/ecodex/telecharger/lirecomp.php",
#               destfile = "COMPO.txt")
rm(list=ls())
load("ANSM.rdata") ## créer par chargement.R
ANSM_COMPO <- ANSM$ANSM_COMPO[,c(1,3:4)]

## ajout code CIP
ANSM_CIP <- ANSM$ANSM_CIP[,c(1,2,7)]

## jointure
ANSM <- merge (ANSM_CIP,ANSM_COMPO,by="CIS")
ANSM$CIS <- NULL
ANSM <- unique(ANSM)

## chargement des alignements
# ces alignements sont créés dans le dossier LinkedThesaurus
alignement <- read.table("RelationThesaurusRepertoireFinal28102016.csv",sep="\t",header=T)
alignement$substances <- NULL
colnames(alignement)
table(alignement$relation)
alignementCIP <- merge (ANSM, alignement, by="code")
# jointure avec les codes CIP de l'ANSM
## on veut le CIP7 :
# alignementCIP <- subset (alignementCIP, select=c("dci","CIP13"))
alignementCIP <- subset (alignementCIP, select=c("dci","CIP7"))
alignementCIP <- unique(alignementCIP)
bool <- is.na(alignementCIP$CIP7)
alignementCIP <- subset (alignementCIP, !bool)
write.table(alignementCIP, "dciCIP7.csv",sep="\t",col.names=F, row.names=F)
