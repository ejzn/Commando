package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

/**
 * Manage the unit
 */
object Units extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Units.list(0, 2, ""))

  /**
   * Describe the unit form (used in both edit and create screens).
   */
  val unitForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "company" -> optional(longNumber)
    )(Unit.apply)(Unit.unapply)
  )

  // -- Actions

  /**
   * Handle default path requests, redirect to unit list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of unit.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on unit names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.unit.list(
      Unit.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  /**
   * Display the 'edit form' of a existing Unit.
   *
   * @param id Id of the unit to edit
   */
  def edit(id: Long) = Action {
    Unit.findById(id).map { unit =>
      Ok(html.unit.editForm(id, unitForm.fill(unit)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the unit to edit
   */
  def update(id: Long) = Action { implicit request =>
    unitForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.unit.editForm(id, formWithErrors)),
      unit => {
        Unit.update(id, unit)
        Home.flashing("success" -> "Unit %s has been updated".format(unit.name))
      }
    )
  }

  /**
   * Display the 'new unit form'.
   */
  def create = Action {
    Ok(html.unit.createForm(unitForm))
  }

  /**
   * Handle the 'new unit form' submission.
   */
  def save = Action { implicit request =>
    unitForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.unit.createForm(formWithErrors)),
      unit => {
        Unit.insert(unit)
        Home.flashing("success" -> "Unit %s has been created".format(unit.name))
      }
    )
  }

  /**
   * Handle unit deletion.
   */
  def delete(id: Long) = Action {
    Unit.delete(id)
    Home.flashing("success" -> "Unit has been deleted")
  }

}

