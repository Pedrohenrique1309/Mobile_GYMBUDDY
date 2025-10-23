package senai.sp.jandira.mobile_gymbuddy.data.model

object IACategoryConstants {
    val categories = listOf(
        IACategory(
            id = "hipertrofia",
            title = "Hipertrofia",
            icon = "muscle",
            description = "Dicas para ganho de massa muscular"
        ),
        IACategory(
            id = "treinos",
            title = "Treinos",
            icon = "fitness",
            description = "Treinos personalizados"
        ),
        IACategory(
            id = "dieta",
            title = "Nutrição",
            icon = "nutrition",
            description = "Orientações sobre alimentação"
        ),
        IACategory(
            id = "mais",
            title = "Mais",
            icon = "more",
            description = "Outras dúvidas sobre fitness"
        )
    )
}
