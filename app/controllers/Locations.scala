package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

/**
 * Manage the location
 */
object Locations extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Locations.list(0, 2, ""))

  /**
   * Describe the location form (used in both edit and create screens).
   */
  val locationForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "location" -> optional(longNumber)
    )(Location.apply)(Location.unapply)
  )

  // -- Actions

  /**
   * Handle default path requests, redirect to location list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of location.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on location names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.location.list(
      Location.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  /**
   * Display the 'edit form' of a existing Location.
   *
   * @param id Id of the location to edit
   */
  def edit(id: Long) = Action {
    Location.findById(id).map { location =>
      Ok(html.location.editForm(id, locationForm.fill(location)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the location to edit
   */
  def update(id: Long) = Action { implicit request =>
    locationForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.location.editForm(id, formWithErrors)),
      location => {
        Location.update(id, location)
        Home.flashing("success" -> "Location %s has been updated".format(location.name))
      }
    )
  }

  /**
   * Display the 'new location form'.
   */
  def create = Action {
    Ok(html.location.createForm(locationForm))
  }

  /**
   * Handle the 'new location form' submission.
   */
  def save = Action { implicit request =>
    locationForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.location.createForm(formWithErrors)),
      location => {
        Location.insert(location)
        Home.flashing("success" -> "Location %s has been created".format(location.name))
      }
    )
  }

  /**
   * Handle location deletion.
   */
  def delete(id: Long) = Action {
    Location.delete(id)
    Home.flashing("success" -> "Location has been deleted")
  }

}

