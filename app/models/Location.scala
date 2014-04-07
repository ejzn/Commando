package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Location(id: Pk[Long] = NotAssigned, name: String, introduced: Option[Date], discontinued: Option[Date], locationId: Option[Long])

object Location {

  // -- Parsers

  /**
   * Parse a Location from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("location.id") ~
    get[String]("location.name") ~
    get[Option[Date]]("location.introduced") ~
    get[Option[Date]]("location.discontinued") ~
    get[Option[Long]]("location.location_id") map {
      case id~name~introduced~discontinued~locationId => Location(id, name, introduced, discontinued, locationId)
    }
  }

  // -- Queries

  /**
   * Retrieve a location from the id.
   */
  def findById(id: Long): Option[Location] = {
    DB.withConnection { implicit connection =>
      SQL("select * from location where id = {id}").on('id -> id).as(Location.simple.singleOpt)
    }
  }

  /**
   * Return a page of (Location,Location).
   *
   * @param page Page to display
   * @param pageSize Number of locations per page
   * @param orderBy Location property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Location)] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val locations = SQL(
        """
          select * from location
          left join location on location.location_id = location.id
          where location.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Location.simple *)

      val totalRows = SQL(
        """
          select count(*) from location
          left join location on location.location_id = location.id
          where location.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(locations, page, offest, totalRows)

    }

  }

  /**
   * Update a location.
   *
   * @param id The location id
   * @param location The location values.
   */
  def update(id: Long, location: Location) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update location
          set name = {name}, introduced = {introduced}, discontinued = {discontinued}, location_id = {location_id}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> location.name,
        'introduced -> location.introduced,
        'discontinued -> location.discontinued,
        'location_id -> location.locationId
      ).executeUpdate()
    }
  }

  /**
   * Insert a new location.
   *
   * @param location The location values.
   */
  def insert(location: Location) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into location values (
            (select next value for location_seq),
            {name}, {introduced}, {discontinued}, {location_id}
          )
        """
      ).on(
        'name -> location.name,
        'introduced -> location.introduced,
        'discontinued -> location.discontinued,
        'location_id -> location.locationId
      ).executeUpdate()
    }
  }

  /**
   * Delete a location.
   *
   * @param id Id of the location to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from location where id = {id}").on('id -> id).executeUpdate()
    }
  }

}

