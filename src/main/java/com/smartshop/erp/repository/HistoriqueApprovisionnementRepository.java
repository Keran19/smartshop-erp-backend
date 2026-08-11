package com.smartshop.erp.repository;

import com.smartshop.erp.entity.HistoriqueApprovisionnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoriqueApprovisionnementRepository extends JpaRepository<HistoriqueApprovisionnement, Long> {

    List<HistoriqueApprovisionnement> findByProduit_IdProduitOrderByDateEntreeDesc(Long idProduit);

    /**
     * Lots d'un produit pour une boutique donnee, du plus ancien au plus recent (ordre FIFO)
     * qui ont encore du stock non ecoule. Le premier de la liste est le lot "actuellement en
     * train d'etre vendu".
     */
    @Query("SELECT h FROM HistoriqueApprovisionnement h WHERE h.produit.idProduit = :idProduit " +
           "AND h.approvisionnement.boutique.idBoutique = :idBoutique AND h.quantiteRestante > 0 " +
           "ORDER BY h.dateEntree ASC")
    List<HistoriqueApprovisionnement> lotsDisponiblesFifo(@Param("idProduit") Long idProduit,
                                                            @Param("idBoutique") Long idBoutique);

    /** Tous les lots (epuises ou non) d'un produit pour une boutique, du plus recent au plus ancien. */
    @Query("SELECT h FROM HistoriqueApprovisionnement h WHERE h.produit.idProduit = :idProduit " +
           "AND h.approvisionnement.boutique.idBoutique = :idBoutique ORDER BY h.dateEntree DESC")
    List<HistoriqueApprovisionnement> tousLesLots(@Param("idProduit") Long idProduit,
                                                    @Param("idBoutique") Long idBoutique);
}
