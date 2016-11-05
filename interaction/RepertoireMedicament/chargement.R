### chargement :
rm(list=ls())
ANSM_CIS <- read.table ("CIS.txt",
                        sep="\t",header=F,
                        quote="\"", fileEncoding = "ISO-8859-9")
colnames(ANSM_CIS) <- c("CIS","princeps","forme","voie","AMM","type","etat","date","statut")
ANSM_CIS <- ANSM_CIS[,-9]

ANSM_CIP <- read.table ("CIS_CIP.txt",
                        sep="\t",header=F,
                        quote="\"", fileEncoding = "ISO-8859-9")

colnames(ANSM_CIP) <-  c("CIS","CIP7","libelle","statut","etat","date","CIP13")
ANSM_CIP$CIP13 <- as.numeric(ANSM_CIP$CIP13)
bool <- is.na(ANSM_CIP$CIP13)
ANSM_CIP$CIP13 <- as.numeric(format (ANSM_CIP$CIP13, digits=13), digits=13)
ANSM_CIP$CIP13[bool] <- NA
ANSM_CIP$CIP13 <- format(ANSM_CIP$CIP13, digits=13)

ANSM_COMPO <- read.table ("COMPO.txt",
                          sep="\t",header=F,
                          quote="\"", fileEncoding = "ISO-8859-9")
colnames(ANSM_COMPO) <- c("CIS","designation","code","substance","dosage","reference",
                          "nature","numero")
ANSM_COMPO <- ANSM_COMPO[,c(1:8)]

##
bool <- ANSM_COMPO$substance == ""
sum(bool)
voir <- subset (ANSM_COMPO, bool)
print(voir)
voir <- subset (ANSM_COMPO, code %in% voir$code)
voir <- voir[order(voir$code),]
voir <- unique(voir)
print (voir, row.names=F)

## Comme on peut le voir, la valeur est vide dans la colonne substance
ANSM_COMPO <- subset (ANSM_COMPO, !bool)
voir <- NULL

ANSM <- list(ANSM_COMPO = ANSM_COMPO, 
             ANSM_CIP = ANSM_CIP,
             ANSM_CIS = ANSM_CIS)
save (ANSM, file="ANSM.rdata")
