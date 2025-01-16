package boo.bloodstone.bloodsex

import boo.bloodstone.bloodsex.animations.blowjob
import boo.bloodstone.bloodsex.animations.doggy
import boo.bloodstone.bloodsex.animations.marry
import com.github.trard.Scheduler

import org.bukkit.entity.Player

enum class Action {
    Bj {
        override val request: String = "предложил сделать вам минет"
        override fun play(firstPlayer: Player, secondPlayer: Player, scheduler: Scheduler) {
            blowjob(firstPlayer, secondPlayer, scheduler)
        }
    },
    Doggy {
        override val request: String = "предложил вам догги-стайл"
        override fun play(firstPlayer: Player, secondPlayer: Player, scheduler: Scheduler) {
            doggy(firstPlayer, secondPlayer, scheduler)
        }
    },
    Marry {
        override val request: String = "сделал вам предложение"
        override fun play(firstPlayer: Player, secondPlayer: Player, scheduler: Scheduler) {
            marry(firstPlayer,secondPlayer,scheduler)
        }
    };

    abstract val request: String
    abstract fun play(firstPlayer: Player, secondPlayer: Player, scheduler: Scheduler)

    companion object {
        fun fromName(name: String): Action? {
            return when (name) {
                "bj" -> Bj
                "doggy" -> Doggy
                "marry" -> Marry
                else -> null
            }
        }
    }
}