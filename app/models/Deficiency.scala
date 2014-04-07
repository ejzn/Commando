package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Deficiency(id: Pk[Long] = NotAssigned, title: String)

object Deficiency {

  // -- Parsers

  /**
   * Parse a Deficiency from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("deficiency.id") ~
    get[String]("deficiency.name") map {
        case id~name => Deficiency(id, name)
        }
    }

  /**
   * Retrieve a deficiency from the id.
   */
  def findById(id: Long): Option[Deficiency] = {
    DB.withConnection { implicit connection =>
      SQL("select * from deficiency where id = {id}").on('id -> id).as(Deficiency.simple.singleOpt)
    }
  }

  /**
   * Return a page of (Deficiency,Company).
   *
   * @param page Page to display
   * @param pageSize Number of deficiencys per page
   * @param orderBy Deficiency property used for sorting
   * @param filter Filter applied on the title column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Deficiency)] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val deficiencys = SQL(
        """
          select * from deficiency
          where deficiency.title like {filter}
          order by {orderBy}
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Deficiency.simple *)

      val totalRows = SQL(
        """
          select count(*) from deficiency
          where deficiency.title like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(deficiencys, page, offest, totalRows)

    }

  }

  /**
   * Update a deficiency.
   *
   * @param id The deficiency id
   * @param deficiency The deficiency values.
   */
  def update(id: Long, deficiency: Deficiency) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update deficiency
          set title = {title}
          where id = {id}
        """
      ).on(
        'id -> id,
        'title -> deficiency.title
      ).executeUpdate()
    }
  }

  /**
   * Insert a new deficiency.
   *
   * @param deficiency The deficiency values.
   */
  def insert(deficiency: Deficiency) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into deficiency values (
            (select next value for deficiency_seq),
            {title}
          )
        """
      ).on(
        'title -> deficiency.title
      ).executeUpdate()
    }
  }

  /**
   * Delete a deficiency.
   *
   * @param id Id of the deficiency to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from deficiency where id = {id}").on('id -> id).executeUpdate()
    }
  }

}

