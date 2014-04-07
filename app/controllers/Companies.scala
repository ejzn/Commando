package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

/**
 * Manage the company
 */
object Companies extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Companies.list(0, 2, ""))

  /**
   * Describe the company form (used in both edit and create screens).
   */
  val companyForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "company" -> optional(longNumber)
    )(Company.apply)(Company.unapply)
  )

  // -- Actions

  /**
   * Handle default path requests, redirect to company list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of company.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on company names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.company.list(
      Company.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  /**
   * Display the 'edit form' of a existing Company.
   *
   * @param id Id of the company to edit
   */
  def edit(id: Long) = Action {
    Company.findById(id).map { company =>
      Ok(html.company.editForm(id, companyForm.fill(company)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the company to edit
   */
  def update(id: Long) = Action { implicit request =>
    companyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.company.editForm(id, formWithErrors)),
      company => {
        Company.update(id, company)
        Home.flashing("success" -> "Company %s has been updated".format(company.name))
      }
    )
  }

  /**
   * Display the 'new company form'.
   */
  def create = Action {
    Ok(html.company.createForm(companyForm))
  }

  /**
   * Handle the 'new company form' submission.
   */
  def save = Action { implicit request =>
    companyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.company.createForm(formWithErrors)),
      company => {
        Company.insert(company)
        Home.flashing("success" -> "Company %s has been created".format(company.name))
      }
    )
  }

  /**
   * Handle company deletion.
   */
  def delete(id: Long) = Action {
    Company.delete(id)
    Home.flashing("success" -> "Company has been deleted")
  }

}

