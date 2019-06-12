package fr.diginamic.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.diginamic.exception.TechnicalException;
import fr.diginamic.utils.ConnectionUtils;

/**
 * Dao gérant les ingrédients
 * 
 * @author Kevin.s
 *
 */
public class IngredientDao {
	/** SERVICE_LOG : Logger */
	private static final Logger SERVICE_LOG = LoggerFactory.getLogger(IngredientDao.class);

	/**
	 * Constructeur
	 * 
	 */
	public IngredientDao() {
		super();
	}

	/**
	 * ajoute tous les ingrédients en base de données
	 * 
	 * @param listeIngredient hashSet des ingrédients
	 */
	public void ajouterAllIngredient(HashSet<String> listeIngredient) {

		PreparedStatement insertIngredient = null;
		try {
			insertIngredient = ConnectionUtils.getInstance()
					.prepareStatement("insert into ingredient (ING_NOM) values (?)");
			for (String ingredient : listeIngredient) {
				insertIngredient.setString(1, ingredient);
				insertIngredient.executeUpdate();
			}
			ConnectionUtils.doCommit();

		} catch (SQLException e) {
			ConnectionUtils.doRollback();
			throw new TechnicalException("probleme d'insertion en base de données", e);
		} finally {
			if (insertIngredient != null) {
				try {
					insertIngredient.close();
				} catch (SQLException e) {
					throw new TechnicalException("impossible de fermer le preparedStatement", e);
				}
			}
			ConnectionUtils.doClose();
		}

	}

	/**
	 * récupère l'identifiant d'un ingrédient par rapport à son nom
	 * 
	 * @param nom nom de l'ingrédients
	 * @return l'identifiant de l'ingrédient
	 */
	public Integer recupererIdIngredient(String nom) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Integer id = null;

		try {
			preparedStatement = ConnectionUtils.getInstance()
					.prepareStatement("select ING_ID from ingredient where ING_NOM=?");
			preparedStatement.setString(1, nom);
			resultSet = preparedStatement.executeQuery();
			ConnectionUtils.doCommit();
			if (resultSet.next()) {
				id = resultSet.getInt("ING_ID");
			}
			return id;
		} catch (SQLException e) {
			SERVICE_LOG.error("probleme de selection en base", e);
			throw new TechnicalException("probleme de selection en base", e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("impossible de fermer le resultSet", e);
					throw new TechnicalException("impossible de fermer le resultSet", e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("impossible de fermer le statement", e);
					throw new TechnicalException("impossible de fermer le statement", e);
				}
			}
			ConnectionUtils.doClose();
		}
	}

	/**
	 * récupère la table d'ingrédient et la stock dans une hashMap avec le nom en
	 * clé et l'identifiant en valeur
	 * 
	 * @return ashMap<String, Integer>
	 */
	public HashMap<String, Integer> recupererAllId() {

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		HashMap<String, Integer> listeIngredient = new HashMap<>();

		try {
			preparedStatement = ConnectionUtils.getInstance().prepareStatement("select * from ingredient");
			resultSet = preparedStatement.executeQuery();
			ConnectionUtils.doCommit();
			while (resultSet.next()) {
				Integer id = resultSet.getInt("ING_ID");
				String nom = resultSet.getString("ING_NOM");
				listeIngredient.put(nom, id);
			}
			return listeIngredient;
		} catch (SQLException e) {
			SERVICE_LOG.error("probleme de selection en base", e);
			throw new TechnicalException("probleme de selection en base", e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("impossible de fermer le resultSet", e);
					throw new TechnicalException("impossible de fermer le resultSet", e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("impossible de fermer le statement", e);
					throw new TechnicalException("impossible de fermer le statement", e);
				}
			}
			ConnectionUtils.doClose();
		}

	}

	public List<String> recupererIngredientsDUnProduit(Integer idDuProduit) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<String> listeDesIngredients = new ArrayList<>();

		// création de la requête requête
		StringBuilder selectQuerySb = new StringBuilder();
		selectQuerySb.append(
				"SELECT ING_NOM FROM ingredient INNER JOIN produit_ingredient ON ingredient.ING_ID = produit_ingredient.PI_IDINGREDIENT INNER JOIN produit ON produit_ingredient.PI_IDPRODUIT = produit.PDT_ID WHERE PI_IDPRODUIT = ");
		selectQuerySb.append(idDuProduit + ";");
		String selectQuery = selectQuerySb.toString();

		try {
			preparedStatement = ConnectionUtils.getInstance().prepareStatement(selectQuery);
			resultSet = preparedStatement.executeQuery();
			ConnectionUtils.doCommit();
			while (resultSet.next()) {
				String ingredientNom = resultSet.getString("ING_NOM");
				listeDesIngredients.add(ingredientNom);
			}
			return listeDesIngredients;
		} catch (SQLException e) {
			SERVICE_LOG.error("probleme de selection en base", e);
			throw new TechnicalException("probleme de selection en base", e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("impossible de fermer le resultSet", e);
					throw new TechnicalException("impossible de fermer le resultSet", e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					SERVICE_LOG.error("impossible de fermer le statement", e);
					throw new TechnicalException("impossible de fermer le statement", e);
				}
			}
			ConnectionUtils.doClose();
		}
	}
}
