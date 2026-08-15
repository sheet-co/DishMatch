# Telegram communication options

Every incoming `Update` has exactly one of the fields below populated — check `hasX()` to find out which, then read the matching getter. Every outgoing option is a method on `TelegramClient` (`client.execute(...)`).

## Incoming: `Update` variants

| Check | Getter | Fires when | Typical use |
|---|---|---|---|
| `hasMessage()` | `getMessage()` | User sends any message (text, photo, command, ...) in a chat with the bot | Normal chat, commands |
| `hasEditedMessage()` | `getEditedMessage()` | User edits a message they already sent | Rare to handle; mostly ignored |
| `hasChannelPost()` / `hasEditedChannelPost()` | `getChannelPost()` | Bot posts to / a post changes in a channel it admins | N/A for DishMatch (no channel use) |
| `hasCallbackQuery()` | `getCallbackQuery()` | User taps an inline keyboard button | Dish-selection buttons (see below) |
| `hasInlineQuery()` | `getInlineQuery()` | User types `@YourBot ...` in *any* chat, not just DishMatch's | "Search-as-you-type" results shown inline elsewhere — not needed unless you want DishMatch usable from other chats |
| `hasChosenInlineResult()` | `getChosenInlineResult()` | User picks a result from an inline query | Only relevant if using inline queries above |
| `hasShippingQuery()` / `hasPreCheckoutQuery()` | `getShippingQuery()` / `getPreCheckoutQuery()` | Steps in Telegram's built-in payments flow | N/A unless DishMatch ever sells something |
| `hasPoll()` / `hasPollAnswer()` | `getPoll()` / `getPollAnswer()` | A poll updates / someone answers one the bot sent | Could work for "rate this dish" instead of buttons |
| `hasMyChatMember()` | `getMyChatMember()` | The bot's own status changes in a chat (blocked, added to a group, ...) | Detect a user blocking the bot |
| `hasChatMember()` | `getChatMember()` | Another member's status changes (needs explicit subscription) | N/A for 1:1 bot chats |
| `hasChatJoinRequest()` | `getChatJoinRequest()` | Someone requests to join a chat the bot admins | N/A — DishMatch isn't a group gatekeeper |

Newer Bot API additions (message reactions, chat boosts, business-account messages) exist too but aren't things DishMatch needs — mentioned here so the table isn't a lie by omission, not because you should reach for them.

**In practice, DishMatch only needs two branches**: `hasMessage()` (already handled in `TelegramCommunication.consume`) and `hasCallbackQuery()` (not handled yet — this is the gap behind the inline-keyboard suggestion).

## Inside `hasMessage()`: what kind of message

| Check | Meaning |
|---|---|
| `getMessage().hasText()` | Plain text — includes commands, see below |
| `getMessage().isCommand()` / text starts with `/` | A slash command like `/start` |
| `getMessage().hasPhoto()` / `hasDocument()` / `hasVoice()` / `hasVideo()` / `hasSticker()` | Media attachments |
| `getMessage().hasLocation()` / `hasContact()` | Shared location / contact card |
| `getMessage().isReply()` / `getReplyToMessage()` | Message sent as a reply to an earlier one — useful for `ForceReply` flows |

For DishMatch, the only ones in play right now are plain text (goes to the AI recommend flow) and — if you add commands — text starting with `/`.

## Outgoing: how the bot responds

| Method | What it does |
|---|---|
| `SendMessage` | Post a new message; optionally carries a `replyMarkup` (see keyboards below) |
| `EditMessageText` | Change the text of a message the bot already sent (e.g. after a button tap) |
| `EditMessageReplyMarkup` | Change just the keyboard on an existing message, leave the text |
| `DeleteMessage` | Remove a message the bot sent |
| `AnswerCallbackQuery` | **Required** ack for any `CallbackQuery` — dismisses the tap's loading spinner; can optionally show a small popup/toast instead of editing the message |
| `AnswerInlineQuery` | Reply to an inline query with a result list |
| `SendChatAction` | Show "typing…" / "uploading…" while the bot works |
| `SetMyCommands` | Register the `/`-menu shown next to the text box (list of `BotCommand{command, description}`) |

## The three `replyMarkup` kinds

| Type | Attaches to | Behaviour on tap |
|---|---|---|
| `InlineKeyboardMarkup` | One specific message | Fires `CallbackQuery`; nothing is typed into the chat |
| `ReplyKeyboardMarkup` | The user's whole input area, persists across messages | Types the button's label as an ordinary text message |
| `ReplyKeyboardRemove` / `ForceReply` | N/A — these clear/prompt rather than show buttons | `ForceReply` makes the client jump straight into "reply" mode, useful for "type your answer now" |

## Decision table for DishMatch's actual scenarios

| You want... | Update type to handle | Reply mechanism |
|---|---|---|
| Bot suggests dishes, user taps one to pick it | `hasCallbackQuery()` (new) | `InlineKeyboardMarkup` on the recommendation message, `AnswerCallbackQuery` + `EditMessageText` on tap |
| `/start`, `/help` etc. as distinct from free chat | `hasMessage()` + text starts with `/` (existing branch, new check inside) | Plain `SendMessage`; optionally `SetMyCommands` once at startup so they show in the native menu |
| Persistent bottom nav (`My Dishes`, `History`, ...) | `hasMessage()`, text matches a known label | `ReplyKeyboardMarkup` sent once (e.g. after `/start`) |
| "Rate this dish 1–5" | `hasPollAnswer()`, or just more `InlineKeyboardMarkup` buttons | `SendPoll` or another inline keyboard |
