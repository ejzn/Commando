package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

/**
 * Manage the building
 */
object Buildings extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Buildings.list(0, 2, ""))

  /**
   * Describe the building form (used in both edit and create screens).
   */
  val buildingForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "title" -> nonEmptyText,
      "location_id" -> optional(longNumber)
    )(Building.apply)(Building.unapply)
  )

  // -- Actions

  /**
   * Handle default path requests, redirect to building list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of building.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on building names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.building.list(
      Building.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  /**
   * Display the 'edit form' of a existing Building.
   *
   * @param id Id of the building to edit
   */
  def edit(id: Long) = Action {
    Building.findById(id).map { building =>
      Ok(html.building.editForm(id, buildingForm.fill(building)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the building to edit
   */
  def update(id: Long) = Action { implicit request =>
    buildingForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.building.editForm(id, formWithErrors)),
      building => {
        Building.update(id, building)
        Home.flashing("success" -> "Building %s has been updated".format(building.title))
      }
    )
  }

  /**
   * Display the 'new building form'.
   */
  def create = Action {
    Ok(html.building.createForm(buildingForm))
  }

  /**
   * Handle the 'new building form' submission.
   */
  def save = Action { implicit request =>
    buildingForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.building.createForm(formWithErrors)),
      building => {
        Building.insert(building)
        Home.flashing("success" -> "Building %s has been created".format(building.title))
      }
    )
  }

  /**
   * Handle building deletion.
   */
  def delete(id: Long) = Action {
    Building.delete(id)
    Home.flashing("success" -> "Building has been deleted")
  }

}

