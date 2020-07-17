package com.vladmykol.takeandcharge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiqPayHistory {
    @JsonIgnore
    @Id
    private String id;
    private int acq_id;
    private String  action; //pay, hold , paysplit, paydonate, auth , regular
    private int agent_commission;
    private int amount;
    private int amount_bonus;
    private int amount_credit;
    private int amount_debit;
    private String authcode_credi;
    private String authcode_debit;
    private String card_token;
    private int commission_credit;
    private int commission_debit;
    private String completion_date;
    private String create_date;
    private String currency;
    private String currency_credit;
    private String currency_debit;
    private String customer; //userId
    private String description;
    private String end_date;
    private String err_code;
    private String err_description;
    private String info;
    private String ip;
    private Boolean is_3ds;
    private String language;
//    Boolean	Возможные значения:
//            true - транзакция прошла с 3DS проверкой,
//false - транзакция прошла без 3DS проверки
    private String liqpay_order_id;
    private int mpi_eci;//	Возможные значения: 5 - транзакция прошла с 3DS (эмитент и эквайер поддерживают технологию 3D-Secure), 6 - эмитент карты плательщика не поддерживает технологию 3D-Secure, 7 - операция прошла без 3D-Secure
    private String order_id;
    private int payment_id;//	Number	Id платежа в системе LiqPay
    private String paytype;//	String	Способ оплаты. Возможные значения card - оплата картой, liqpay - через кабинет liqpay, privat24 - через кабинет приват24, masterpass - через кабинет masterpass, moment_part - рассрочка, cash - наличными, invoice - счет на e-mail, qr - сканирование qr-кода.
    private String public_key;
    private int receiver_commission;
    private String redirect_to;//	String	Ссылка на которую необходимо перенаправить клиента для прохождения 3DS верификации
    private String refund_date_last;
    private String rrn_credit;
    private String rrn_debit;
    private int sender_bonus;
    private String sender_card_bank;
    private String sender_card_country;
    private String sender_card_mask2;
    private String sender_card_type;
    private int sender_commission;
    private String sender_first_name;
    private String sender_last_name;
    private String sender_phone;
    private String status;
//    Возможные значения:
//    Конечные статусы платежа
//    error	Неуспешный платеж. Некорректно заполнены данные
//    failure	Неуспешный платеж
//    reversed	Платеж возвращен
//    subscribed	Подписка успешно оформлена
//    success	Успешный платеж
//    unsubscribed	Подписка успешно деактивирована
//    Cтатусы требующие подтверждения платежа
//3ds_verify	Требуется 3DS верификация.
//    Для завершения платежа, требуется выполнить 3ds_verify
//    captcha_verify	Ожидается подтверждение captcha
//    cvv_verify	Требуется ввод CVV карты отправителя.
//    Для завершения платежа, требуется выполнить cvv_verify
//    ivr_verify	Ожидается подтверждение звонком ivr
//    otp_verify	Требуется OTP подтверждение клиента. OTP пароль отправлен на номер телефона Клиента.
//    Для завершения платежа, требуется выполнить otp_verify
//    password_verify	Ожидается подтверждение пароля приложения Приват24
//    phone_verify	Ожидается ввод телефона клиентом.
//    Для завершения платежа, требуется выполнить phone_verify
//    pin_verify	Ожидается подтверждение pin-code
//    receiver_verify	Требуется ввод данных получателя.
//    Для завершения платежа, требуется выполнить receiver_verify
//    sender_verify	Требуется ввод данных отправителя.
//    Для завершения платежа, требуется выполнить sender_verify
//    senderapp_verify	Ожидается подтверждение в приложении SENDER
//    wait_qr	Ожидается сканирование QR-кода клиентом
//    wait_sender	Ожидается подтверждение оплаты клиентом в приложении Privat24/SENDER
//    Другие статусы платежа
//    cash_wait	Ожидается оплата наличными в ТСО
//    hold_wait	Сумма успешно заблокирована на счету отправителя
//    invoice_wait	Инвойс создан успешно, ожидается оплата
//    prepared	Платеж создан, ожидается его завершение отправителем
//    processing	Платеж обрабатывается
//    wait_accept	Деньги с клиента списаны, но магазин еще не прошел проверку. Если магазин не пройдет активацию в течение 180 дней, платежи будут автоматически отменены
//    wait_card	Не установлен способ возмещения у получателя
//    wait_compensation	Платеж успешный, будет зачислен в ежесуточной проводке
//    wait_lc	Аккредитив. Деньги с клиента списаны, ожидается подтверждение доставки товара
//    wait_reserve	Средства по платежу зарезервированы для проведения возврата по ранее поданной заявке
//    wait_secure	Платеж на проверке
    private String token;
    private String transaction_id;
    private String type;
    private int version;
    private String err_erc;
    private String product_category;
    private String product_description;
    private String product_name;
    private String product_url;
    private int refund_amount;
    private String verifycode;
}
