/**
 * Общее ядро «Памятно»: единый формат ошибок, безопасность, время и контракты
 * интеграционных событий. Единственный модуль, от которого могут зависеть остальные.
 */
@ApplicationModule(displayName = "Общее ядро", type = ApplicationModule.Type.OPEN)
package net.pamytno.common;

import org.springframework.modulith.ApplicationModule;
