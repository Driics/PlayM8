package ru.driics.playm8.domain.repository

import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase

interface DatabaseRepository {

    val gamesDatabase: QuerySnapshot

}