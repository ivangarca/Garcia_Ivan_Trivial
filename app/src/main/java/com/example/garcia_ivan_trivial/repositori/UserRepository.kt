package com.example.garcia_ivan_trivial.repositori


import com.example.garcia_ivan_trivial.dao.UserDao
import com.example.garcia_ivan_trivial.model.User

object UserRepository {

    suspend fun prepararDadesDeProva(dao: UserDao) {
        if (dao.getUser("a") == null) {
            dao.insert(User("a", "a", 0)) //
        }
        if (dao.getUser("b") == null) {
            dao.insert(User("b", "b", 0)) //
        }
    }

    suspend fun addUser(user: User, dao: UserDao): Boolean {
        val result = dao.insert(user)
        // Si result és -1, significa que hi ha hagut un error (per exemple, usuari repetit).
        return result != -1L
    }

    // Busca un usuari pel seu nom.
    suspend fun getUser(username: String, dao: UserDao): User? {
        return dao.getUser(username)
    }

    // Obté el rànquing dels 5 millors jugadors.
    suspend fun getTop5(dao: UserDao): List<User> {
        return dao.getTop5Users()
    }

    // Actualitza el número de victòries si el nou resultat és millor.
    suspend fun updateVictorias(user: User, nuevasVictorias: Int, dao: UserDao) {
        if (nuevasVictorias > user.victorias) {
            val updatedUser = user.copy(victorias = nuevasVictorias)
            dao.update(updatedUser)
        }
    }
}