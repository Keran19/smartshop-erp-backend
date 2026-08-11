package com.smartshop.erp.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartshop.erp.entity.Facture;
import com.smartshop.erp.entity.LigneVente;
import com.smartshop.erp.entity.Vente;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.repository.ParametreRepository;
import com.smartshop.erp.service.FacturePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Genere le PDF du recu/facture de caisse pour une vente, au format d'un ticket de caisse
 * thermique (58mm ou 80mm selon le parametre "largeur_ticket_mm", 80mm par defaut).
 * La hauteur de la page est calculee dynamiquement en fonction du nombre d'articles pour
 * ne pas gaspiller de rouleau avec une page trop haute (contrairement a un format A5 fixe).
 */
@Service
@RequiredArgsConstructor
public class FacturePdfServiceImpl implements FacturePdfService {

    private final ParametreRepository parametreRepository;

    @Value("${app.fichiers.dossier-factures}")
    private String dossierFactures;

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MM_VERS_PT = 2.834645f;

    // Hauteur estimee (en mm) pour l'en-tete + infos vente + totaux + pied de page,
    // a laquelle s'ajoute une hauteur par ligne d'article.
    private static final float HAUTEUR_BASE_MM = 85f;
    private static final float HAUTEUR_PAR_LIGNE_MM = 6.5f;
    private static final float HAUTEUR_MIN_MM = 100f;

    @Override
    public String genererPdfVente(Vente vente, Facture facture) {
        try {
            File dossier = new File(dossierFactures);
            if (!dossier.exists() && !dossier.mkdirs()) {
                throw new OperationInvalideException("Impossible de creer le dossier de sortie des factures : " + dossierFactures);
            }

            String cheminFichier = dossierFactures + File.separator + facture.getNumeroFacture() + ".pdf";

            float largeurMm = largeurTicketMm();
            float largeurPt = largeurMm * MM_VERS_PT;
            int nombreLignes = vente.getLignes() != null ? vente.getLignes().size() : 0;
            float hauteurMm = Math.max(HAUTEUR_MIN_MM, HAUTEUR_BASE_MM + nombreLignes * HAUTEUR_PAR_LIGNE_MM);
            float hauteurPt = hauteurMm * MM_VERS_PT;
            PageSize taillePage = new PageSize(largeurPt, hauteurPt);

            // Marges plus etroites qu'en A5 : un rouleau thermique n'a pas de marge a gaspiller.
            float margePt = (largeurMm <= 58f ? 3f : 5f) * MM_VERS_PT;

            try (PdfWriter writer = new PdfWriter(cheminFichier);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, taillePage)) {

                document.setMargins(margePt, margePt, margePt, margePt);

                String nomBoutique = parametre("nom_boutique", "SmartShop");
                String adresseBoutique = parametre("adresse", "");
                String telephoneBoutique = parametre("telephone", "");
                String devise = parametre("devise", "FCFA");
                String messageTicket = parametre("message_ticket", "Merci de votre confiance.");

                // En-tete
                document.add(new Paragraph(nomBoutique)
                        .setBold().setFontSize(largeurMm <= 58f ? 12 : 14).setTextAlignment(TextAlignment.CENTER));
                if (!adresseBoutique.isBlank()) {
                    document.add(new Paragraph(adresseBoutique)
                            .setFontSize(7).setTextAlignment(TextAlignment.CENTER));
                }
                if (!telephoneBoutique.isBlank()) {
                    document.add(new Paragraph("Tel : " + telephoneBoutique)
                            .setFontSize(7).setTextAlignment(TextAlignment.CENTER));
                }
                document.add(new Paragraph(vente.getBoutique().getNom())
                        .setFontSize(7).setItalic().setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph(" ").setFontSize(3));
                document.add(ligneSeparation(largeurMm));

                // Infos vente
                document.add(new Paragraph("Facture N\u00b0 : " + facture.getNumeroFacture()).setFontSize(7).setBold());
                document.add(new Paragraph("Vente N\u00b0 : " + vente.getNumeroVente()).setFontSize(7));
                document.add(new Paragraph("Date : " + vente.getDateVente().format(FORMAT_DATE)).setFontSize(7));
                document.add(new Paragraph("Vendeur : " + vente.getVendeur().getNom() + " " + vente.getVendeur().getPrenom()).setFontSize(7));
                if (vente.getClient() != null) {
                    document.add(new Paragraph("Client : " + vente.getClient().getNom()
                            + (vente.getClient().getPrenom() != null ? " " + vente.getClient().getPrenom() : "")).setFontSize(7));
                }

                document.add(ligneSeparation(largeurMm));

                // Tableau des lignes
                Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1.5f, 1.5f})).useAllAvailableWidth();
                ajouterEnteteCellule(table, "Article");
                ajouterEnteteCellule(table, "Qte");
                ajouterEnteteCellule(table, "P.U.");
                ajouterEnteteCellule(table, "Total");

                for (LigneVente ligne : vente.getLignes()) {
                    table.addCell(celluleSimple(ligne.getProduit().getNom(), TextAlignment.LEFT));
                    table.addCell(celluleSimple(String.valueOf(ligne.getQuantite()), TextAlignment.CENTER));
                    table.addCell(celluleSimple(formatMontant(ligne.getPrixUnitaire()), TextAlignment.RIGHT));
                    table.addCell(celluleSimple(formatMontant(ligne.getSousTotal()), TextAlignment.RIGHT));
                }
                document.add(table);

                document.add(ligneSeparation(largeurMm));

                // Totaux
                Table totaux = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
                totaux.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                ajouterLigneTotal(totaux, "Montant total", vente.getMontantTotal(), devise, false);
                if (vente.getRemiseGlobale() != null && vente.getRemiseGlobale().compareTo(BigDecimal.ZERO) > 0) {
                    ajouterLigneTotal(totaux, "Remise", vente.getRemiseGlobale(), devise, false);
                }
                ajouterLigneTotal(totaux, "A PAYER", vente.getMontantFinal(), devise, true);
                if (vente.getMontantRecu() != null) {
                    ajouterLigneTotal(totaux, "Recu", vente.getMontantRecu(), devise, false);
                    ajouterLigneTotal(totaux, "Rendu", vente.getMonnaieRendue(), devise, false);
                }
                document.add(totaux);

                document.add(new Paragraph("Reglement : " + libelleModeReglement(vente.getModeReglement().name()))
                        .setFontSize(7).setMarginTop(4));

                document.add(ligneSeparation(largeurMm));
                document.add(new Paragraph(messageTicket)
                        .setFontSize(7).setItalic().setTextAlignment(TextAlignment.CENTER).setMarginTop(6));
            }

            return cheminFichier;

        } catch (IOException e) {
            throw new OperationInvalideException("Erreur lors de la generation du PDF de la facture : " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------

    /** Largeur du rouleau thermique en mm, reglable via le parametre "largeur_ticket_mm" (58 ou 80). Defaut : 80. */
    private float largeurTicketMm() {
        String valeur = parametre("largeur_ticket_mm", "80");
        try {
            float largeur = Float.parseFloat(valeur.trim());
            return largeur > 0 ? largeur : 80f;
        } catch (NumberFormatException e) {
            return 80f;
        }
    }

    private String parametre(String cle, String defaut) {
        Optional<com.smartshop.erp.entity.Parametre> p = parametreRepository.findByCleParametre(cle);
        return p.map(com.smartshop.erp.entity.Parametre::getValeur).filter(v -> v != null && !v.isBlank()).orElse(defaut);
    }

    /** Ligne de tirets dimensionnee a la largeur reelle du ticket (evite le retour a la ligne sur 58mm). */
    private Paragraph ligneSeparation(float largeurMm) {
        int nbTirets = Math.max(16, Math.round(largeurMm * 0.55f));
        return new Paragraph("-".repeat(nbTirets))
                .setFontSize(7).setTextAlignment(TextAlignment.CENTER).setMarginTop(2).setMarginBottom(2);
    }

    private void ajouterEnteteCellule(Table table, String texte) {
        Cell cell = new Cell().add(new Paragraph(texte).setBold().setFontSize(7));
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        cell.setPadding(2);
        table.addHeaderCell(cell);
    }

    private Cell celluleSimple(String texte, TextAlignment align) {
        Cell cell = new Cell().add(new Paragraph(texte).setFontSize(7).setTextAlignment(align));
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(2);
        return cell;
    }

    private void ajouterLigneTotal(Table table, String libelle, BigDecimal montant, String devise, boolean accent) {
       Paragraph p1 = new Paragraph(libelle).setFontSize(accent ? 9 : 7);
        if (accent) {
         p1.setBold();
        }
        Cell c1 = new Cell().add(p1);

        Paragraph p2 = new Paragraph(formatMontant(montant) + " " + devise)
            .setFontSize(accent ? 9 : 7)
            .setTextAlignment(TextAlignment.RIGHT);
        if (accent) {
         p2.setBold();
        }
        Cell c2 = new Cell().add(p2);
        c1.setBorder(Border.NO_BORDER);
        c2.setBorder(Border.NO_BORDER);
        table.addCell(c1);
        table.addCell(c2);
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) montant = BigDecimal.ZERO;
        return String.format("%,.0f", montant).replace(",", " ");
    }

    private String libelleModeReglement(String mode) {
        return switch (mode) {
            case "COMPTANT" -> "Comptant";
            case "CREDIT" -> "Credit";
            default -> mode;
        };
    }
}
