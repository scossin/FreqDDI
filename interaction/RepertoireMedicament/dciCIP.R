## Date de mise à jour 03/10/2016
### téléchargement du fichier sur le site de l'ANSM : 
# download.file(url="http://agence-prd.ansm.sante.fr/php/ecodex/telecharger/lirecomp.php",
#               destfile = "COMPO.txt")
rm(list=ls())
load("ANSM.rdata") ## créer par chargement.R
ANSM_CIS <- ANSM$ANSM_CIS
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

########################################## familles sans molecule : 
library(IMthesaurusANSM)
df <- thesaurus72013
df$check_decompose()
## médicaments par voie orale
bool <- grepl("orale",ANSM_CIS$voie)
voir <- subset (ANSM_CIS, bool)
voir <- merge (voir, ANSM_CIP, by="CIS")
voir <- subset (voir, select=c(CIP7))
voir <- unique(voir)
voir$dci <- "MEDICAMENTS ADMINISTRES PAR VOIE ORALE"
alignementCIP <- rbind (alignementCIP, voir)

## voie vaginale : 
bool <- grepl("vaginal",ANSM_CIS$voie)
voir <- subset (ANSM_CIS, bool)
voir <- merge (voir, ANSM_CIP, by="CIS")
voir <- subset (voir, select=c(CIP7))
voir <- unique(voir)
voir$dci <- "MEDICAMENTS UTILISES PAR VOIE VAGINALE"
alignementCIP <- rbind (alignementCIP, voir)

## sels de fer par voie injectable 
voies <- unique(ANSM_CIS$voie)
voies <- as.character(voies)
voies <- unlist(lapply(voies, function(x){
  unlist(strsplit(x, ";"))
}))
voies <- unique(voies)
## pour le fer
voiesinjectables <- c("sous-cutanée","intraveineuse","intramusculaire","voie parentérale autre")
voiesinjectables <- paste(voiesinjectables, collapse="|")
CISCOMPO <- merge (ANSM_CIS, ANSM_COMPO, by="CIS")
codeFer <- subset (alignement, dci == "FER")
bool <- grepl(voiesinjectables, CISCOMPO$voie) & CISCOMPO$code %in% codeFer$code
voir <- subset (CISCOMPO, bool)
voir <- merge (voir, ANSM_CIP, by="CIS")
voir <- subset (voir, select=c(CIP7))
voir <- unique(voir)
voir$dci <- "SELS DE FER PAR VOIE INJECTABLE"
alignementCIP <- rbind (alignementCIP, voir)
write.table(alignementCIP, "dciCIP7.csv",sep="\t",col.names=F, row.names=F)
