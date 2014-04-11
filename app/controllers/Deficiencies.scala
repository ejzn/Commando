package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

/**
 * Manage the deficiency
 */
object Deficiencies extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Deficiencies.list(0, 2, ""))

  /**
   * Describe the deficiency form (used in both edit and create screens).
   */
  val deficiencyForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "title" -> nonEmptyText
    )(Deficiency.apply)(Deficiency.unapply)
  )

  // -- Actions

  /**
   * Handle default path requests, redirect to deficiency list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of deficiency.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on deficiency names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.deficiency.list(
      Deficiency.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  /**
   * Display the 'edit form' of a existing Deficiency.
   *
   * @param id Id of the deficiency to edit
   */
  def edit(id: Long) = Action {
    Deficiency.findById(id).map { deficiency =>
      Ok(html.deficiency.editForm(id, deficiencyForm.fill(deficiency)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the deficiency to edit
   */
  def update(id: Long) = Action { implicit request =>
    deficiencyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.deficiency.editForm(id, formWithErrors)),
      deficiency => {
        Deficiency.update(id, deficiency)
        Home.flashing("success" -> "Deficiency %s has been updated".format(deficiency.title))
      }
    )
  }

  /**
   * Display the 'new deficiency form'.
   */
  def create = Action {
    Ok(html.deficiency.createForm(deficiencyForm, Unit.options(1)))
  }

  /**
   * Handle the 'new deficiency form' submission.
   */
  def save = Action { implicit request =>
    deficiencyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.deficiency.createForm(formWithErrors, Unit.options(1))),
      deficiency => {
        Deficiency.insert(deficiency)
        Home.flashing("success" -> "Deficiency %s has been created".format(deficiency.title))
      }
    )
  }

  /**
   * Handle deficiency deletion.
   */
  def delete(id: Long) = Action {
    Deficiency.delete(id)
    Home.flashing("success" -> "Deficiency has been deleted")
  }

}

