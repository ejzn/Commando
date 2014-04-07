package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Company(id: Pk[Long] = NotAssigned, name: String)
case class Unit(id: Pk[Long] = NotAssigned, name: String, introduced: Option[Date], discontinued: Option[Date], companyId: Option[Long])

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Unit {

  // -- Parsers

  /**
   * Parse a Unit from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("unit.id") ~
    get[String]("unit.name") ~
    get[Option[Date]]("unit.introduced") ~
    get[Option[Date]]("unit.discontinued") ~
    get[Option[Long]]("unit.company_id") map {
      case id~name~introduced~discontinued~companyId => Unit(id, name, introduced, discontinued, companyId)
    }
  }

  /**
   * Parse a (Unit,Company) from a ResultSet
   */
  val withCompany = Unit.simple ~ (Company.simple ?) map {
    case unit~company => (unit,company)
  }

  // -- Queries

  /**
   * Retrieve a unit from the id.
   */
  def findById(id: Long): Option[Unit] = {
    DB.withConnection { implicit connection =>
      SQL("select * from unit where id = {id}").on('id -> id).as(Unit.simple.singleOpt)
    }
  }

  /**
   * Return a page of (Unit,Company).
   *
   * @param page Page to display
   * @param pageSize Number of units per page
   * @param orderBy Unit property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Unit, Option[Company])] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val units = SQL(
        """
          select * from unit
          left join company on unit.company_id = company.id
          where unit.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Unit.withCompany *)

      val totalRows = SQL(
        """
          select count(*) from unit
          left join company on unit.company_id = company.id
          where unit.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(units, page, offest, totalRows)

    }

  }

  /**
   * Update a unit.
   *
   * @param id The unit id
   * @param unit The unit values.
   */
  def update(id: Long, unit: Unit) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update unit
          set name = {name}, introduced = {introduced}, discontinued = {discontinued}, company_id = {company_id}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> unit.name,
        'introduced -> unit.introduced,
        'discontinued -> unit.discontinued,
        'company_id -> unit.companyId
      ).executeUpdate()
    }
  }

  /**
   * Insert a new unit.
   *
   * @param unit The unit values.
   */
  def insert(unit: Unit) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into unit values (
            (select next value for unit_seq),
            {name}, {introduced}, {discontinued}, {company_id}
          )
        """
      ).on(
        'name -> unit.name,
        'introduced -> unit.introduced,
        'discontinued -> unit.discontinued,
        'company_id -> unit.companyId
      ).executeUpdate()
    }
  }

  /**
   * Delete a unit.
   *
   * @param id Id of the unit to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from unit where id = {id}").on('id -> id).executeUpdate()
    }
  }

}

object Company {

  /**
   * Parse a Company from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("company.id") ~
    get[String]("company.name") map {
      case id~name => Company(id, name)
    }
  }

  /**
   * Construct the Map[String,String] needed to fill a select options set.
   */
  def options: Seq[(String,String)] = DB.withConnection { implicit connection =>
    SQL("select * from company order by name").as(Company.simple *).map(c => c.id.toString -> c.name)
  }

}

