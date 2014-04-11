package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Building(id: Pk[Long] = NotAssigned, title: String, locationId: Option[Long])

object Building {

  // -- Parsers

  /**
   * Parse a Building from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("building.id") ~
    get[String]("building.title") ~
    get[Option[Long]]("building.location_id") map {
      case id~title~locationId => Building(id, title, locationId)
    }
  }

  // -- Queries

  /**
   * Retrieve a building from the id.
   */
  def findById(id: Long): Option[Building] = {
    DB.withConnection { implicit connection =>
      SQL("select * from building where id = {id}").on('id -> id).as(Building.simple.singleOpt)
    }
  }

  /**
   * Return a page of (Building,Building).
   *
   * @param page Page to display
   * @param pageSize Number of buildings per page
   * @param orderBy Building property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Building)] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val buildings = SQL(
        """
          select * from building
          where building.title like {filter}
          order by {orderBy}
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Building.simple *)

      val totalRows = SQL(
        """
          select count(*) from building
          where building.title like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(buildings, page, offest, totalRows)

    }

  }

  /**
   * Update a building.
   *
   * @param id The building id
   * @param building The building values.
   */
  def update(id: Long, building: Building) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update building
          set title = {name}, location_id = {location_id}
          where id = {id}
        """
      ).on(
        'id -> id,
        'title -> building.title,
        'location_id -> building.locationId
      ).executeUpdate()
    }
  }

  /**
   * Insert a new building.
   *
   * @param building The building values.
   */
  def insert(building: Building) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into building values (
            (select next value for building_seq),
            {title}, {introduced}, {discontinued}, {building_id}
          )
        """
      ).on(
        'title -> building.title,
        'location_id -> building.locationId
      ).executeUpdate()
    }
  }

  /**
   * Delete a building.
   *
   * @param id Id of the building to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from building where id = {id}").on('id -> id).executeUpdate()
    }
  }

  /**
   * Construct the Map[String,String] needed to fill a select options set.
   */
  def options(company_id: Int): Seq[(String, String)] = DB.withConnection { implicit connection =>
      SQL("select * from building where location_id = {company_id} order by title").on('company_id -> company_id).as(Unit.simple *).map(c => c.id.toString -> c.number.toString)
  }


}

