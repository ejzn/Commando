package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Company(id: Pk[Long] = NotAssigned, name: String, introduced: Option[Date], discontinued: Option[Date], companyId: Option[Long])

object Company {

  // -- Parsers

  /**
   * Parse a Company from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("company.id") ~
    get[String]("company.name") ~
    get[Option[Date]]("company.introduced") ~
    get[Option[Date]]("company.discontinued") ~
    get[Option[Long]]("company.company_id") map {
      case id~name~introduced~discontinued~companyId => Company(id, name, introduced, discontinued, companyId)
    }
  }

  // -- Queries

  /**
   * Retrieve a company from the id.
   */
  def findById(id: Long): Option[Company] = {
    DB.withConnection { implicit connection =>
      SQL("select * from company where id = {id}").on('id -> id).as(Company.simple.singleOpt)
    }
  }

  /**
   * Return a page of (Company).
   *
   * @param page Page to display
   * @param pageSize Number of companys per page
   * @param orderBy Company property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Company)] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val companys = SQL(
        """
          select * from company
          left join company on company.company_id = company.id
          where company.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Company.simple *)

      val totalRows = SQL(
        """
          select count(*) from company
          left join company on company.company_id = company.id
          where company.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(companys, page, offest, totalRows)

    }

  }

  /**
   * Update a company.
   *
   * @param id The company id
   * @param company The company values.
   */
  def update(id: Long, company: Company) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update company
          set name = {name}, introduced = {introduced}, discontinued = {discontinued}, company_id = {company_id}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> company.name,
        'introduced -> company.introduced,
        'discontinued -> company.discontinued,
        'company_id -> company.companyId
      ).executeUpdate()
    }
  }

  /**
   * Insert a new company.
   *
   * @param company The company values.
   */
  def insert(company: Company) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into company values (
            (select next value for company_seq),
            {name}, {introduced}, {discontinued}, {company_id}
          )
        """
      ).on(
        'name -> company.name,
        'introduced -> company.introduced,
        'discontinued -> company.discontinued,
        'company_id -> company.companyId
      ).executeUpdate()
    }
  }

  /**
   * Delete a company.
   *
   * @param id Id of the company to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from company where id = {id}").on('id -> id).executeUpdate()
    }
  }

}

