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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartshop.erp.entity.Vente;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.repository.ParametreRepository;
import com.smartshop.erp.service.RapportVentesPdfService;
import com.smartshop.erp.util.GenerateurNumero;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/** Genere un PDF au format A4 paysage recapitulant la liste des ventes d'une periode donnee. */
@Service
@RequiredArgsConstructor
public class RapportVentesPdfServiceImpl implements RapportVentesPdfService {

    private final ParametreRepository parametreRepository;

    @Value("${app.fichiers.dossier-factures}")
    private String dossierFactures;

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMAT_JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String genererPdfListeVentes(List<Vente> ventes, LocalDateTime debut, LocalDateTime fin, Long idBoutique) {
        try {
            File dossier = new File(dossierFactures);
            if (!dossier.exists() && !dossier.mkdirs()) {
                throw new OperationInvalideException("Impossible de creer le dossier de sortie : " + dossierFactures);
            }

            String nomFichier = GenerateurNumero.generer("RAPPORT_VENTES") + ".pdf";
            String cheminFichier = dossierFactures + File.separator + nomFichier;

            try (PdfWriter writer = new PdfWriter(cheminFichier);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4.rotate())) {

                document.setMargins(25, 25, 25, 25);

                String nomBoutique = parametre("nom_boutique", "SmartShop");
                String devise = parametre("devise", "FCFA");

                document.add(new Paragraph(nomBoutique + " - Rapport des ventes")
                        .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Periode du " + debut.format(FORMAT_JOUR) + " au " + fin.format(FORMAT_JOUR))
                        .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Genere le " + LocalDateTime.now().format(FORMAT_DATE))
                        .setFontSize(8).setItalic().setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

                Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2.5f, 2, 2.5f, 2, 1.5f, 1.5f, 1.5f, 1.5f}))
                        .useAllAvailableWidth();

                for (String entete : new String[]{"N\u00b0 Vente", "Date", "Boutique", "Client", "Vendeur",
                        "Montant total", "Remise", "Montant final", "Benefice"}) {
                    Cell cell = new Cell().add(new Paragraph(entete).setBold().setFontSize(8).setFontColor(ColorConstants.WHITE));
                    cell.setBackgroundColor(ColorConstants.DARK_GRAY);
                    cell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
                    table.addHeaderCell(cell);
                }

                BigDecimal totalMontant = BigDecimal.ZERO;
                BigDecimal totalRemise = BigDecimal.ZERO;
                BigDecimal totalFinal = BigDecimal.ZERO;
                BigDecimal totalBenefice = BigDecimal.ZERO;

                for (Vente vente : ventes) {
                    BigDecimal benefice = vente.getBenefice();
                    totalMontant = totalMontant.add(vente.getMontantTotal());
                    totalRemise = totalRemise.add(vente.getRemiseGlobale() == null ? BigDecimal.ZERO : vente.getRemiseGlobale());
                    totalFinal = totalFinal.add(vente.getMontantFinal());
                    totalBenefice = totalBenefice.add(benefice);

                    table.addCell(cellule(vente.getNumeroVente(), TextAlignment.LEFT));
                    table.addCell(cellule(vente.getDateVente().format(FORMAT_DATE), TextAlignment.LEFT));
                    table.addCell(cellule(vente.getBoutique().getNom(), TextAlignment.LEFT));
                    table.addCell(cellule(vente.getClient() != null
                            ? (vente.getClient().getNom() + " " + (vente.getClient().getPrenom() != null ? vente.getClient().getPrenom() : "")).trim()
                            : "Client de passage", TextAlignment.LEFT));
                    table.addCell(cellule(vente.getVendeur().getNom() + " " + vente.getVendeur().getPrenom(), TextAlignment.LEFT));
                    table.addCell(cellule(formatMontant(vente.getMontantTotal()), TextAlignment.RIGHT));
                    table.addCell(cellule(formatMontant(vente.getRemiseGlobale()), TextAlignment.RIGHT));
                    table.addCell(cellule(formatMontant(vente.getMontantFinal()), TextAlignment.RIGHT));
                    table.addCell(cellule(formatMontant(benefice), TextAlignment.RIGHT));
                }
                document.add(table);

                document.add(new Paragraph(" ").setFontSize(6));

                Table totaux = new Table(UnitValue.createPercentArray(new float[]{2, 2})).useAllAvailableWidth();
                ajouterTotal(totaux, "Nombre de ventes", String.valueOf(ventes.size()));
                ajouterTotal(totaux, "Chiffre d'affaires total", formatMontant(totalFinal) + " " + devise);
                ajouterTotal(totaux, "Total des remises", formatMontant(totalRemise) + " " + devise);
                ajouterTotal(totaux, "Benefice total", formatMontant(totalBenefice) + " " + devise);
                document.add(totaux);
            }

            return cheminFichier;

        } catch (IOException e) {
            throw new OperationInvalideException("Erreur lors de la generation du rapport PDF : " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------

    private String parametre(String cle, String defaut) {
        Optional<com.smartshop.erp.entity.Parametre> p = parametreRepository.findByCleParametre(cle);
        return p.map(com.smartshop.erp.entity.Parametre::getValeur).filter(v -> v != null && !v.isBlank()).orElse(defaut);
    }

    private Cell cellule(String texte, TextAlignment align) {
        Cell cell = new Cell().add(new Paragraph(texte == null ? "" : texte).setFontSize(7.5f).setTextAlignment(align));
        cell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.3f));
        return cell;
    }

    private void ajouterTotal(Table table, String libelle, String valeur) {
        Cell c1 = new Cell().add(new Paragraph(libelle).setBold().setFontSize(9));
        Cell c2 = new Cell().add(new Paragraph(valeur).setBold().setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
        c1.setBorder(Border.NO_BORDER);
        c2.setBorder(Border.NO_BORDER);
        table.addCell(c1);
        table.addCell(c2);
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) montant = BigDecimal.ZERO;
        return String.format("%,.0f", montant).replace(",", " ");
    }
}
