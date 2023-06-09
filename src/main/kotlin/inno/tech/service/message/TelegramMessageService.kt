package inno.tech.service.message

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.model.User
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.text.MessageFormat

@Service
class TelegramMessageService(
    private val telegramBotApi: TelegramBotApi,
) : MessageService {

    override fun sendMessage(chatId: String, template: String, args: Array<Any>) {
        val message = SendMessage()
        message.text = MessageFormat.format(template, *args)
        message.parseMode = ParseMode.MARKDOWN
        message.chatId = chatId
        message.allowSendingWithoutReply = false

        telegramBotApi.execute(message)
    }

    override fun sendErrorMessage(chatId: Long) {
        sendMessageWithKeyboard(chatId.toString(), MAIN_MENU, Message.ERROR)
    }

    override fun sendMessageWithKeyboard(chatId: String, replyMarkup: InlineKeyboardMarkup, template: String, args: Array<Any>) {
        val message = SendMessage()
        message.text = MessageFormat.format(template, *args)
        message.parseMode = ParseMode.MARKDOWN
        message.chatId = chatId
        message.replyMarkup = replyMarkup

        telegramBotApi.execute(message)
    }

    override fun sendProfileInfoMessage(user: User) {
        val fullName = user.fullName ?: DATA_IS_NOT_DEFINED
        val city = user.city ?: DATA_IS_NOT_DEFINED
        val profileUrl = user.profileUrl ?: DATA_IS_NOT_DEFINED

        sendMessageWithKeyboard(user.chatId.toString(), contactPartnerBtn(user), Message.PROFILE, arrayOf(fullName, city, profileUrl))
    }

    override fun sendInventionMessage(user: User, partner: User) {

        val profileUrl = partner.profileUrl ?: DATA_IS_NOT_DEFINED
        val fullName = partner.fullName ?: DATA_IS_NOT_DEFINED
        val city = partner.city ?: DATA_IS_NOT_DEFINED

        sendMessageWithKeyboard(user.chatId.toString(), contactPartnerBtn(user), Message.MATCH_INVITATION, arrayOf(fullName, city, profileUrl))
    }

    private fun contactPartnerBtn(partner: User): InlineKeyboardMarkup {
        val contactPartner = InlineKeyboardButton().apply {
            text = "Telegram ${partner.fullName}"
            url = "tg://user?id=${partner.userId}"
        }
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(contactPartner),
            )
        }
    }


    companion object {
        /** Значение по умолчанию */
        const val DATA_IS_NOT_DEFINED = "Не определено"

        /** Главное меню приложения */
        val MAIN_MENU: InlineKeyboardMarkup = createMainMenu()

        private fun createMainMenu() = InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(actionBtn("Что такое Random coffee", Command.INFO)),
                listOf(actionBtn("Посмотреть свой профиль", Command.SHOW_PROFILE)),
                listOf(actionBtn("Поменять данные профиля", Command.EDIT_PROFILE)),
                listOf(actionBtn("Поставить бот на паузу", Command.PAUSE)),
                listOf(actionBtn("Снять бота с паузы", Command.RESUME)),
            )
        }

        private fun actionBtn(name: String, command: Command) = InlineKeyboardButton().apply {
            text = name
            callbackData = command.command
        }
    }
}
