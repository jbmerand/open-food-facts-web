package fr.diginamic.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.diginamic.exception.TechnicalException;
import fr.diginamic.model.Produit;
import fr.diginamic.utils.ConnectionUtils;

/**
 * Dao gérant les produits
 *
 */
public class ProduitDao {

	/** SERVICE_LOG : Logger */
	private static final Logger SERVICE_LOG = LoggerFactory.getLogger(ProduitDao.class);

	/**
	 * Constructeur
	 * 
	 */
	public ProduitDao() {
		super();
	}

	/**
	 * Récupère la liste des grades nutritionnels de la base de données et la
	 * retourne.
	 * 
	 * @return : List<String> liste sans doublons des grades
	 */
	public List<String> recupererDifferentsGradesNutritionnels() {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<String> listeGrades = new ArrayList<>();

		try {
			preparedStatement = ConnectionUtils.getInstance().prepareStatement(
					"SELECT DISTINCT PDT_NUTRITIONGRADE FROM openfoodfactbdd.produit ORDER BY PDT_NUTRITIONGRADE ASC");
			resultSet = preparedStatement.executeQuery();
			ConnectionUtils.doCommit();

			while (resultSet.next()) {
				String nom = resultSet.getString("PDT_NUTRITIONGRADE");
				listeGrades.add(nom);
			}
			return listeGrades;

		} catch (SQLException e) {
			SERVICE_LOG.error("Problème de sélection dans la base de données.");
			throw new TechnicalException("Problème de sélection dans la base de données.");
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("Impossible de fermer le resultSet.", e);
					throw new TechnicalException("Impossible de fermer le resultSet.", e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("Impossible de fermer le statement.", e);
					throw new TechnicalException("Impossible de fermer le statement.", e);
				}
			}
			ConnectionUtils.doClose();
		}
	}

	/**
	 * Retourne une liste des produits contenant leurs informations essentielles.
	 * 
	 * @param criteres : Map<String, String> les tables concernées
	 * @return : List<Produit> liste des produits
	 */
	public List<Produit> recupererProduits(Map<String, String> criteres) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Produit> listeProduits = new ArrayList<>();

		StringBuilder selectQuerySb = new StringBuilder();
		selectQuerySb.append(
				"SELECT pro.*, mar.MRQ_NOM, cat.CTG_NOM FROM produit pro INNER JOIN marque mar ON pro.PDT_MARQUE = mar.MRQ_ID INNER JOIN categorie cat ON pro.PDT_CATEGORIE = cat.CTG_ID WHERE ");

		if (criteres.containsKey("PDT_NOM")) {
			selectQuerySb.append("PDT_NOM = " + "\"" + criteres.get("PDT_NOM") + "\"");
			selectQuerySb.append(" AND ");
		}
		if (criteres.containsKey("PDT_CATEGORIE")) {
			selectQuerySb.append("PDT_CATEGORIE = " + "\"" + criteres.get("PDT_CATEGORIE") + "\"");
			selectQuerySb.append(" AND ");
		}
		if (criteres.containsKey("PDT_MARQUE")) {
			selectQuerySb.append("PDT_MARQUE = " + "\"" + criteres.get("PDT_MARQUE") + "\"");
			selectQuerySb.append(" AND ");
		}
		if (criteres.containsKey("PDT_NUTRITIONGRADE")) {
			selectQuerySb.append("PDT_NUTRITIONGRADE = " + "\"" + criteres.get("PDT_NUTRITIONGRADE") + "\"");
			selectQuerySb.append(" AND ");
		}
		if (criteres.containsKey("PDT_ENERGIE_MIN") && criteres.containsKey("PDT_ENERGIE_MAX")) {
			selectQuerySb.append("(PDT_ENERGIE BETWEEN " + criteres.get("PDT_ENERGIE_MIN") + " AND "
					+ criteres.get("PDT_ENERGIE_MAX") + ")");
			selectQuerySb.append(" AND ");
		}
		if (criteres.containsKey("PDT_GRAISSE_MIN") && criteres.containsKey("PDT_GRAISSE_MAX")) {
			selectQuerySb.append("(PDT_GRAISSE BETWEEN " + criteres.get("PDT_GRAISSE_MIN") + " AND "
					+ criteres.get("PDT_GRAISSE_MAX") + ")");
			selectQuerySb.append(" AND ");
		}
		selectQuerySb.append("PDT_FIBRE >= 0 LIMIT 100;");
		String selectQuery = selectQuerySb.toString();

		try {
			preparedStatement = ConnectionUtils.getInstance().prepareStatement(selectQuery);
			resultSet = preparedStatement.executeQuery();
			ConnectionUtils.doCommit();

			while (resultSet.next()) {
				String nom = resultSet.getString("PDT_NOM");
				String grade = resultSet.getString("PDT_NUTRITIONGRADE");
				String marque = resultSet.getString("MRQ_NOM");
				String categorie = resultSet.getString("CTG_NOM");
				Double graisse = resultSet.getDouble("PDT_GRAISSE");
				Double energie = resultSet.getDouble("PDT_ENERGIE");
				Integer id = resultSet.getInt("PDT_ID");

				Produit produit = new Produit(id, nom, categorie, grade, marque, energie, graisse);

				listeProduits.add(produit);
			}
			return listeProduits;

		} catch (SQLException e) {
			SERVICE_LOG.error("Problème de sélection dans la base de données.");
			throw new TechnicalException("Problème de sélection dans la base de données.");
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("Impossible de fermer le resultSet.", e);
					throw new TechnicalException("Impossible de fermer le resultSet.", e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("Impossible de fermer le statement.", e);
					throw new TechnicalException("Impossible de fermer le statement.", e);
				}
			}
			ConnectionUtils.doClose();
		}
	}

}
