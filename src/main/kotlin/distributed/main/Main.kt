package distributed.main
import distributed.dao.Database
import distributed.dto.NodeStatusDto
import distributed.util.LoadUtil
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.Javalin
import java.io.ByteArrayInputStream

fun main() {
    AppState.load()

    val database = Database()

    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, _ -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("not found") }
    }.start(AppState.port)

    app.routes {

        get("/status") { ctx ->
            ctx.json(NodeStatusDto(AppState.nodeName, LoadUtil.getFreeSpacePercentage(), AppState.itemGroups.values.toList()))
        }

        get("/collections/:name") { ctx ->
            ctx.json(database.findByName(ctx.pathParam("name"))!!)
        }

        get("/collections/:name/query") { ctx ->
            val itemQuery = ctx.body()
            ctx.json(database.queryItemGroup(ctx.pathParam("name"), itemQuery)!!)
        }

        post("/collections/:name") { ctx ->
            val item = ctx.body()
            database.save(name = ctx.pathParam("name"), item = item)
            ctx.status(201)
        }

        put("/collections/:name/item/:id") { ctx ->
            val item = ctx.body()
            database.update(ctx.pathParam("name"), ctx.pathParam("id"), item)
        }

        delete("/collections/:name/query") { ctx ->
            val itemQuery = ctx.body()
            database.deleteFromItemGroup(itemGroupName = ctx.pathParam("name"), itemQuery = itemQuery)
            ctx.status(204)
        }

        delete("/collections/:name") { ctx ->
            database.deleteItemGroup(ctx.pathParam("name"))
            ctx.status(204)
        }

        post("/collections/:name/copy") {ctx ->
            val itemGroupsRequestMetadata = ctx.body()
            database.importItemGroup(itemGroupsRequestMetadata)
            ctx.status(201)
        }

        get("/collections/:name/copy") {ctx ->
            val itemGroupName = ctx.pathParam("name")
            val zippedFile = database.getItemGroupFile(itemGroupName)
            ctx.header("Content-Disposition", "attachment; $itemGroupName.zip")
            ctx.result(ByteArrayInputStream(zippedFile.toByteArray()))
        }
    }
}
