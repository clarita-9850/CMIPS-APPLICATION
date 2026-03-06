package com.cmips.config;

import com.cmips.entity.NoaCategoryMessageEntity;
import com.cmips.repository.NoaCategoryMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds NOA category code messages from DSD Appendix G into the database on startup.
 * All 27 category groups with ~130 codes are loaded if the table is empty.
 *
 * Categories (DSD Appendix G):
 *   AA – Provisional Disability/Blindness
 *   AP – Advance Payment
 *   AR – Alternative Resource
 *   AS – Additional Assistance
 *   DN – Denial Reasons
 *   FF – Free-Form Text
 *   FR – Funding Reduction
 *   FS – Funding Source / Program Transfer
 *   HR – Hours Reassessment
 *   IN – Legal Services Notice
 *   LM – Legislative Mandate Reduction
 *   LV – Leave / Not in Own Home
 *   MS – Medical Social Worker Service
 *   OT – Other
 *   PM – Protective Monitoring
 *   PR – Program Related
 *   PS – Protective Supervision
 *   RH – Respite Hours
 *   RM – Reduction / Modification
 *   RS – Rescind
 *   SC – Share of Cost
 *   SD – Service Discontinuation
 *   SH – State Hearing
 *   SP – Special Circumstances
 *   TR – Termination
 *   UN – Undetermined
 *   VS – Various / Miscellaneous
 */
@Component
public class NoaCategoryMessageDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NoaCategoryMessageDataLoader.class);

    private final NoaCategoryMessageRepository repository;

    public NoaCategoryMessageDataLoader(NoaCategoryMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            log.debug("NOA category messages already seeded ({} rows), skipping.", repository.count());
            return;
        }
        log.info("Seeding NOA category messages from DSD Appendix G...");
        List<NoaCategoryMessageEntity> all = new ArrayList<>();

        // ─────────────────────────────────────────────────────────────────────
        // AA — Provisional Disability / Blindness (DSD Appendix G, Group AA)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("AA01", "AA", "Provisional Disability/Blindness Approval",
            "You are being approved on a provisional basis for IHSS services pending the establishment of your permanent disability or blindness. This provisional approval is effective {DATE}. A final determination will be made within the time required by law.",
            "Usted está siendo aprobado(a) provisionalmente para los servicios de IHSS pendiente del establecimiento de su discapacidad o ceguera permanente. Esta aprobación provisional es efectiva el {DATE}. Se tomará una determinación final dentro del tiempo requerido por la ley.",
            "您正在暂时获得IHSS服务批准，等待确认您的永久残疾或失明。此临时批准自{DATE}起生效。最终决定将在法律规定的时间内做出。",
            "Դուք ժամանակավոր հիմունքներով հաստատվում եք IHSS ծառայությունների համար՝ սպասելով Ձեր մշտական հաշմանդամության կամ կուրության հաստատմանը: Այս ժամանակավոր հաստատումն ուժի մեջ է {DATE}-ից:",
            true));

        all.add(msg("AA02", "AA", "Final Approval of Prior Provisional",
            "Your provisional approval for IHSS services has been finalized. Your permanent approval is effective {DATE}. The authorized services described in this notice will continue.",
            "Su aprobación provisional para los servicios de IHSS ha sido finalizada. Su aprobación permanente es efectiva el {DATE}. Los servicios autorizados descritos en este aviso continuarán.",
            "您对IHSS服务的临时批准已经最终确定。您的永久批准自{DATE}起生效。本通知中描述的授权服务将继续。",
            "Ձեր IHSS ծառայությունների ժամանակավոր հաստատումը վերջնականացվել է: Ձեր մշտական հաստատումն ուժի մեջ է {DATE}-ից:",
            true));

        all.add(msg("AA03", "AA", "Application Previously Denied in Error",
            "Your application for IHSS services was previously denied in error. We are approving your application effective {DATE}. We apologize for the inconvenience this error may have caused.",
            "Su solicitud de servicios de IHSS fue denegada anteriormente por error. Estamos aprobando su solicitud a partir del {DATE}. Nos disculpamos por los inconvenientes que este error pudo haber causado.",
            "您的IHSS服务申请此前因错误而被拒绝。我们自{DATE}起批准您的申请。对于此错误可能给您带来的不便，我们深表歉意。",
            "Ձեր IHSS ծառայությունների դիմումը նախկինում սխալմամբ մերժվել էր: Մենք հաստատում ենք Ձեր դիմումը {DATE}-ից: Կներեք այս սխալի պատճառած անհարմարության համար:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // AP — Advance Payment (DSD Appendix G, Group AP)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("AP01", "AP", "Advance Payment - Approval",
            "You have been approved to receive an advance payment for IHSS services. The advance payment in the amount of {AMOUNT} will be issued to you effective {DATE}.",
            "Usted ha sido aprobado(a) para recibir un pago anticipado por los servicios de IHSS. El pago anticipado por la cantidad de {AMOUNT} le será emitido a partir del {DATE}.",
            "您已获批接受IHSS服务的预付款。预付款金额为{AMOUNT}，将于{DATE}发放给您。",
            "Ձեզ հաստատվել է IHSS ծառայությունների կանխավճար ստանալու համար: {AMOUNT} գումարի կանխավճարը կտրամադրվի Ձեզ {DATE}-ից:",
            true));

        all.add(msg("AP02", "AP", "Advance Payment - Denial",
            "Your request for an advance payment for IHSS services has been denied. You do not meet the eligibility requirements for advance payment.",
            "Su solicitud de pago anticipado por servicios de IHSS ha sido denegada. Usted no cumple con los requisitos de elegibilidad para el pago anticipado.",
            "您申请IHSS服务预付款的请求已被拒绝。您不符合预付款的资格要求。",
            "IHSS ծառայությունների կանխավճարի Ձեր հայտն մերժվել է: Դուք չեք բավարարում կանխավճարի իրավունքի պահանջներին:",
            false));

        all.add(msg("AP03", "AP", "Advance Payment - Reconciliation Required",
            "Your advance payment must be reconciled. The advance payment of {AMOUNT} issued on {DATE} has not been reconciled within 45 days. Please contact your county office immediately.",
            "Su pago anticipado debe ser conciliado. El pago anticipado de {AMOUNT} emitido el {DATE} no ha sido conciliado dentro de los 45 días. Por favor comuníquese con la oficina del condado inmediatamente.",
            "您的预付款必须核对。{DATE}发放的{AMOUNT}预付款在45天内未进行核对。请立即联系您所在县的办公室。",
            "Ձեր կանխավճարը պետք է հաշտեցվի: {DATE}-ին տրված {AMOUNT} կանխավճարը 45 օրվա ընթացքում չի հաշտեցվել: Խնդրում ենք անմիջապես կապ հաստատել կոմսության գրասենյակի հետ:",
            true));

        all.add(msg("AP04", "AP", "Advance Payment - Not Reconciled 75 Days",
            "Your advance payment of {AMOUNT} issued on {DATE} has not been reconciled within 75 days. Failure to reconcile this advance payment may result in a reduction or termination of your IHSS services.",
            "Su pago anticipado de {AMOUNT} emitido el {DATE} no ha sido conciliado dentro de los 75 días. El no conciliar este pago anticipado puede resultar en una reducción o terminación de sus servicios de IHSS.",
            "您于{DATE}收到的{AMOUNT}预付款在75天内未进行核对。未能核对此预付款可能导致您的IHSS服务减少或终止。",
            "Ձեր {DATE}-ին տրված {AMOUNT} կանխավճարը 75 օրվա ընթացքում չի հաշտեցվել: Այս կանխավճարը չհաշտեցնելը կարող է հանգեցնել Ձեր IHSS ծառայությունների կրճատման կամ դադարեցման:",
            true));

        all.add(msg("AP05", "AP", "Advance Payment - Rate Change",
            "Your advance payment rate has changed effective {DATE}. Your new advance payment rate is {AMOUNT} per month.",
            "Su tasa de pago anticipado ha cambiado a partir del {DATE}. Su nueva tasa de pago anticipado es de {AMOUNT} por mes.",
            "您的预付款率自{DATE}起已更改。您的新预付款率为每月{AMOUNT}。",
            "Ձեր կանխավճարի դրույքաչափը փոփոխվել է {DATE}-ից: Ձեր նոր կանխավճարի դրույքաչափը {AMOUNT} է ամսական:",
            true));

        all.add(msg("AP06", "AP", "Advance Payment - Provider Change",
            "Your advance payment provider has changed effective {DATE}. Please ensure your new provider is aware of the advance payment requirements.",
            "Su proveedor de pago anticipado ha cambiado a partir del {DATE}. Por favor asegúrese de que su nuevo proveedor conozca los requisitos del pago anticipado.",
            "您的预付款提供者自{DATE}起已更改。请确保您的新提供者了解预付款要求。",
            "Ձեր կանխավճարի մատակարարը փոխվել է {DATE}-ից: Խնդրում ենք համոզվել, որ Ձեր նոր մատակարարը տեղյակ է կանխավճարի պահանջներին:",
            true));

        all.add(msg("AP07", "AP", "Advance Payment - Termination",
            "Your advance payment for IHSS services will be discontinued effective {DATE}.",
            "Su pago anticipado por servicios de IHSS será discontinuado a partir del {DATE}.",
            "您的IHSS服务预付款将于{DATE}起停止。",
            "Ձեր IHSS ծառայությունների կանխավճարը կդադարի {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // AR — Alternative Resource Authorization Changes (Group AR)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("AR01", "AR", "Alternative Resource - Hours Reduced",
            "Your authorized IHSS hours have been reduced effective {DATE} because an alternative resource is available to meet some of your needs. Your authorized hours have been reduced from {HOURS} to {HOURS} per month.",
            "Sus horas autorizadas de IHSS han sido reducidas a partir del {DATE} porque hay un recurso alternativo disponible para satisfacer algunas de sus necesidades.",
            "由于有替代资源可以满足您的部分需求，您的授权IHSS小时数自{DATE}起已减少。",
            "Ձեր թույլատրված IHSS ժամերը կրճատվել են {DATE}-ից, քանի որ Ձեր կարիքների մի մասը բավարարելու համար հասանելի է այլընտրանքային ռեսուրս:",
            true));

        all.add(msg("AR02", "AR", "Alternative Resource - Service Discontinued",
            "Your authorized IHSS service for {SERVICES} has been discontinued effective {DATE} because an alternative resource is available.",
            "Su servicio de IHSS autorizado para {SERVICES} ha sido discontinuado a partir del {DATE} porque hay un recurso alternativo disponible.",
            "由于有替代资源可用，您针对{SERVICES}的授权IHSS服务已于{DATE}起停止。",
            "Ձեր {SERVICES}-ի թույլատրված IHSS ծառայությունը դադարեցվել է {DATE}-ից, քանի որ հասանելի է այլընտրանքային ռեսուրս:",
            true));

        all.add(msg("AR03", "AR", "Alternative Resource - Reassessment Required",
            "A reassessment of your IHSS needs is required because the availability of alternative resources has changed. Please contact your county social worker to schedule a reassessment.",
            "Se requiere una reevaluación de sus necesidades de IHSS porque la disponibilidad de recursos alternativos ha cambiado. Por favor comuníquese con su trabajador social del condado para programar una reevaluación.",
            "由于替代资源的可用性发生了变化，需要重新评估您的IHSS需求。请联系您所在县的社会工作者安排重新评估。",
            "Ձեր IHSS կարիքների վերագնահատումն անհրաժեշտ է, քանի որ այլընտրանքային ռեսուրսների հասանելիությունը փոխվել է:",
            false));

        // ─────────────────────────────────────────────────────────────────────
        // AS — Additional Assistance (Group AS)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("AS01", "AS", "Request for Additional Assistance",
            "If you need help understanding this notice, or if you need services in a different language or format (large print, Braille, audio), please contact your county IHSS office at the address or phone number shown on this notice.",
            "Si necesita ayuda para entender este aviso, o si necesita servicios en un idioma o formato diferente (letra grande, Braille, audio), por favor comuníquese con la oficina de IHSS de su condado.",
            "如果您需要帮助理解此通知，或者您需要不同语言或格式的服务（大字体、盲文、音频），请联系您所在县的IHSS办公室。",
            "Եթե Ձեզ անհրաժեշտ է օգնություն այս ծանուցումը հասկանալու համար, կամ եթե Ձեզ անհրաժեշտ են ծառայություններ այլ լեզվով կամ ձևաչափով, խնդրում ենք կապ հաստատել Ձեր կոմսության IHSS գրասենյակի հետ:",
            false));

        // ─────────────────────────────────────────────────────────────────────
        // DN — Denial Reasons (Group DN, DSD Appendix G)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("DN01", "DN", "Denial - SSI/SSP Board and Care",
            "Your application for IHSS services has been denied because you are receiving SSI/SSP and residing in a Board and Care facility. Board and Care residents are not eligible for IHSS.",
            "Su solicitud de servicios de IHSS ha sido denegada porque recibe SSI/SSP y reside en una instalación de Board and Care. Los residentes de Board and Care no son elegibles para IHSS.",
            "您的IHSS服务申请被拒绝，因为您正在接受SSI/SSP并居住在寄宿护理机构。寄宿护理居民没有资格获得IHSS。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ստանում եք SSI/SSP և ապրում եք Board and Care հաստատությունում:",
            false));

        all.add(msg("DN02", "DN", "Denial - Citizenship/Immigration Status",
            "Your application for IHSS services has been denied because you do not meet the citizenship or immigration status requirements for IHSS.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no cumple con los requisitos de ciudadanía o estado migratorio para IHSS.",
            "您的IHSS服务申请已被拒绝，因为您不符合IHSS的公民身份或移民身份要求。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք չեք բավարարում IHSS-ի քաղաքացիության կամ ներգաղթային կարգավիճակի պահանջներին:",
            false));

        all.add(msg("DN03", "DN", "Denial - California Residency",
            "Your application for IHSS services has been denied because you do not meet the California residency requirement. You must be a resident of California to receive IHSS.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no cumple con el requisito de residencia en California. Debe ser residente de California para recibir IHSS.",
            "您的IHSS服务申请被拒绝，因为您不符合加利福尼亚州居住要求。您必须是加利福尼亚州居民才能获得IHSS。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք չեք բավարարում Կալիֆոռնիայի բնակության պահանջը:",
            false));

        all.add(msg("DN04", "DN", "Denial - Not Living in Own Home",
            "Your application for IHSS services has been denied because you are not living in your own home. IHSS is only available to people living in their own homes.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no vive en su propio hogar. IHSS solo está disponible para personas que viven en su propio hogar.",
            "您的IHSS服务申请被拒绝，因为您没有住在自己的家里。IHSS只向居住在自己家中的人提供。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ապրում եք ոչ Ձեր սեփական տանը: IHSS-ը հասանելի է միայն սեփական տանն ապրող անձանց:",
            false));

        all.add(msg("DN05", "DN", "Denial - Whereabouts Unknown",
            "Your application for IHSS services has been denied because your whereabouts are unknown.",
            "Su solicitud de servicios de IHSS ha sido denegada porque se desconoce su paradero.",
            "您的IHSS服务申请被拒绝，因为您的下落不明。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Ձեր գտնվելու վայրն անհայտ է:",
            false));

        all.add(msg("DN06", "DN", "Denial - Hospitalized",
            "Your application for IHSS services has been denied because you are currently hospitalized. IHSS services are not provided to persons in hospitals.",
            "Su solicitud de servicios de IHSS ha sido denegada porque actualmente está hospitalizado(a). Los servicios de IHSS no se proporcionan a personas en hospitales.",
            "您的IHSS服务申请被拒绝，因为您目前正在住院。IHSS服务不向住院患者提供。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ներկայումս հոսպիտալացված եք:",
            false));

        all.add(msg("DN07", "DN", "Denial - Skilled Nursing Facility",
            "Your application for IHSS services has been denied because you are residing in a Skilled Nursing Facility.",
            "Su solicitud de servicios de IHSS ha sido denegada porque reside en una Instalación de Enfermería Especializada.",
            "您的IHSS服务申请被拒绝，因为您居住在专业护理机构。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ապրում եք Մասնագիտական բուժքույրական կենտրոնում:",
            false));

        all.add(msg("DN08", "DN", "Denial - Intermediate Care Facility",
            "Your application for IHSS services has been denied because you are residing in an Intermediate Care Facility.",
            "Su solicitud de servicios de IHSS ha sido denegada porque reside en una Instalación de Cuidado Intermedio.",
            "您的IHSS服务申请被拒绝，因为您居住在中级护理机构。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ապրում եք Միջանկյալ բուժօգնության հաստատությունում:",
            false));

        all.add(msg("DN09", "DN", "Denial - Community Care Facility",
            "Your application for IHSS services has been denied because you are residing in a Community Care Facility.",
            "Su solicitud de servicios de IHSS ha sido denegada porque reside en una Instalación de Cuidado Comunitario.",
            "您的IHSS服务申请被拒绝，因为您居住在社区护理机构。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ապրում եք Համայնքային խնամքի հաստատությունում:",
            false));

        all.add(msg("DN10", "DN", "Denial - Death",
            "This case has been closed due to the death of the recipient.",
            "Este caso ha sido cerrado debido al fallecimiento del beneficiario.",
            "此案例因受益人去世而关闭。",
            "Այս գործը փակվել է շահառուի մահվան պատճառով:",
            false));

        all.add(msg("DN11", "DN", "Denial - Invalid Social Security Number",
            "Your application for IHSS services has been denied because you have not provided a valid Social Security Number or Individual Taxpayer Identification Number.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no ha proporcionado un Número de Seguro Social o Número de Identificación de Contribuyente Individual válido.",
            "您的IHSS服务申请被拒绝，因为您没有提供有效的社会安全号码或个人纳税人识别号码。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք չեք ներկայացրել վավեր Սոցիալական ապահովության համար:",
            false));

        all.add(msg("DN12", "DN", "Denial - Duplicate Social Security Number",
            "Your application for IHSS services has been denied because the Social Security Number you provided is already associated with another case.",
            "Su solicitud de servicios de IHSS ha sido denegada porque el Número de Seguro Social que proporcionó ya está asociado con otro caso.",
            "您的IHSS服务申请被拒绝，因为您提供的社会安全号码已与另一个案例关联。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Ձեր ներկայացրած Սոցիալական ապահովության համարն արդեն կապված է մեկ այլ գործի հետ:",
            false));

        all.add(msg("DN13", "DN", "Denial - Medical Certification Not Received",
            "Your application for IHSS services has been denied because the required medical certification (SOC 873) was not received.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no se recibió la certificación médica requerida (SOC 873).",
            "您的IHSS服务申请被拒绝，因为未收到所需的医疗证明（SOC 873）。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ չի ստացվել պահանջվող բժշկական վկայագիրը (SOC 873):",
            false));

        all.add(msg("DN14", "DN", "Denial - Medical Certification Insufficient",
            "Your application for IHSS services has been denied because the medical certification received does not establish your need for IHSS services.",
            "Su solicitud de servicios de IHSS ha sido denegada porque la certificación médica recibida no establece su necesidad de servicios de IHSS.",
            "您的IHSS服务申请被拒绝，因为收到的医疗证明未能证明您对IHSS服务的需求。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ ստացված բժշկական վկայագիրը չի հաստատում IHSS ծառայությունների Ձեր կարիքը:",
            false));

        all.add(msg("DN15", "DN", "Denial - PACE Enrolled",
            "Your application for IHSS services has been denied because you are enrolled in the PACE (Program of All-Inclusive Care for the Elderly) program.",
            "Su solicitud de servicios de IHSS ha sido denegada porque está inscrito(a) en el programa PACE (Programa de Atención Integral para Personas Mayores).",
            "您的IHSS服务申请被拒绝，因为您已加入PACE（老年人全面护理计划）项目。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք ընդգրկված եք PACE ծրագրում:",
            false));

        all.add(msg("DN16", "DN", "Denial - No Functional Need",
            "Your application for IHSS services has been denied because the assessment indicates you do not have a functional need for IHSS services at this time.",
            "Su solicitud de servicios de IHSS ha sido denegada porque la evaluación indica que actualmente no tiene una necesidad funcional de los servicios de IHSS.",
            "您的IHSS服务申请被拒绝，因为评估表明您目前在功能上不需要IHSS服务。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ գնահատումը ցույց է տալիս, որ Դուք ներկայումս ֆունկցիոնալ կարիք չունեք IHSS ծառայությունների:",
            false));

        all.add(msg("DN17", "DN", "Denial - Medi-Cal Ineligible",
            "Your application for IHSS services has been denied because you are not eligible for Medi-Cal. You must be eligible for Medi-Cal to receive IHSS services.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no es elegible para Medi-Cal. Debe ser elegible para Medi-Cal para recibir servicios de IHSS.",
            "您的IHSS服务申请被拒绝，因为您没有资格获得Medi-Cal。您必须有资格获得Medi-Cal才能获得IHSS服务。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք Medi-Cal-ի իրավունք չունեք:",
            false));

        all.add(msg("DN18", "DN", "Denial - Age Requirement Not Met",
            "Your application for IHSS services has been denied because you do not meet the age requirement.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no cumple con el requisito de edad.",
            "您的IHSS服务申请被拒绝，因为您不符合年龄要求。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք չեք բավարարում տարիքային պահանջը:",
            false));

        all.add(msg("DN19", "DN", "Denial - Income Exceeds Limit",
            "Your application for IHSS services has been denied because your income exceeds the eligibility limit.",
            "Su solicitud de servicios de IHSS ha sido denegada porque sus ingresos superan el límite de elegibilidad.",
            "您的IHSS服务申请被拒绝，因为您的收入超过了资格限制。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Ձեր եկամուտը գերազանցում է իրավունքի սահմանաչափը:",
            false));

        all.add(msg("DN20", "DN", "Denial - No Response to Home Visit",
            "Your application for IHSS services has been denied because there was no response to scheduled home visit attempts.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no hubo respuesta a los intentos de visita al hogar programados.",
            "您的IHSS服务申请被拒绝，因为对预约家访尝试没有回应。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ պատրաստված տնային այցի փորձերին պատասխան չկա:",
            false));

        all.add(msg("DN21", "DN", "Denial - Authorized Representative Not Designated",
            "Your application for IHSS services requires an authorized representative to be designated. No authorized representative has been identified.",
            "Su solicitud de servicios de IHSS requiere que se designe un representante autorizado. No se ha identificado ningún representante autorizado.",
            "您的IHSS服务申请需要指定一名授权代表。尚未确定授权代表。",
            "Ձեր IHSS ծառայությունների դիմումն պահանջում է, որ նշանակվի լիազոր ներկայացուցիչ: Լիազոր ներկայացուցիչ չի հայտնաբերվել:",
            false));

        all.add(msg("DN22", "DN", "Denial - Application Withdrawn",
            "Your application for IHSS services has been closed because you withdrew your application.",
            "Su solicitud de servicios de IHSS ha sido cerrada porque retiró su solicitud.",
            "您的IHSS服务申请已关闭，因为您撤回了申请。",
            "Ձեր IHSS ծառայությունների դիմումը փակվել է, քանի որ Դուք հետ կանչեցիք Ձեր դիմումը:",
            false));

        all.add(msg("DN23", "DN", "Denial - Failure to Provide Information",
            "Your application for IHSS services has been denied because you failed to provide required information within the specified time period.",
            "Su solicitud de servicios de IHSS ha sido denegada porque no proporcionó la información requerida dentro del período de tiempo especificado.",
            "您的IHSS服务申请被拒绝，因为您未在规定时间内提供所需信息。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է, քանի որ Դուք սահմանված ժամկետում չեք ներկայացրել պահանջվող տեղեկությունները:",
            false));

        all.add(msg("DN24", "DN", "Denial - Other Reason",
            "Your application for IHSS services has been denied.",
            "Su solicitud de servicios de IHSS ha sido denegada.",
            "您的IHSS服务申请已被拒绝。",
            "Ձեր IHSS ծառայությունների դիմումը մերժվել է:",
            false));

        // ─────────────────────────────────────────────────────────────────────
        // FF — Free-Form Text (max 200 characters) (Group FF)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("FF01", "FF", "Free-Form Text Notice",
            "{TEXT}",
            "{TEXT}",
            "{TEXT}",
            "{TEXT}",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // FR — Funding Reduction (Group FR)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("FR01", "FR", "Funding Reduction - State Budget",
            "Your IHSS hours are being reduced effective {DATE} due to a reduction in state funding. Your authorized hours will be reduced from {HOURS} to {HOURS} per month.",
            "Sus horas de IHSS se están reduciendo a partir del {DATE} debido a una reducción en el financiamiento estatal.",
            "由于州政府资金削减，您的IHSS小时数自{DATE}起减少。",
            "Ձեր IHSS ժամերը կրճատվում են {DATE}-ից՝ պետական ֆինանսավորման կրճատման պատճառով:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // FS — Funding Source / Program Transfer (Group FS)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("FS01", "FS", "Transfer to IPO Program",
            "Your IHSS case is being transferred to the IPO (Independent Provider Option) funding program effective {DATE}.",
            "Su caso de IHSS está siendo transferido al programa de financiamiento IPO (Opción de Proveedor Independiente) a partir del {DATE}.",
            "您的IHSS案例自{DATE}起转入IPO（独立提供者选项）资助项目。",
            "Ձեր IHSS գործը {DATE}-ից փոխանցվում է IPO (Անկախ Մատակարար Տարբերակ) ֆինանսավորման ծրագրին:",
            true));

        all.add(msg("FS02", "FS", "Transfer to PCSP Program",
            "Your IHSS case is being transferred to the PCSP (Personal Care Services Program) effective {DATE}. Your authorized hours of {HOURS} per month will continue under the new program.",
            "Su caso de IHSS está siendo transferido al PCSP (Programa de Servicios de Cuidado Personal) a partir del {DATE}.",
            "您的IHSS案例自{DATE}起转入PCSP（个人护理服务计划）。",
            "Ձեր IHSS գործը {DATE}-ից փոխանցվում է PCSP (Անձնական Խնամքի Ծառայությունների Ծրագիր) ծրագրին:",
            true));

        all.add(msg("FS03", "FS", "Transfer to IHSS-R Program",
            "Your IHSS case is being transferred to the IHSS-Residual (IHSS-R) program effective {DATE}.",
            "Su caso de IHSS está siendo transferido al programa IHSS-Residual (IHSS-R) a partir del {DATE}.",
            "您的IHSS案例自{DATE}起转入IHSS-剩余（IHSS-R）项目。",
            "Ձեր IHSS գործը {DATE}-ից փոխանցվում է IHSS-Residual (IHSS-R) ծրագրին:",
            true));

        all.add(msg("FS04", "FS", "Transfer to CFCO Program",
            "Your IHSS case is being transferred to the CFCO (County-Funded County Operations) program effective {DATE}.",
            "Su caso de IHSS está siendo transferido al programa CFCO (Operaciones del Condado Financiadas por el Condado) a partir del {DATE}.",
            "您的IHSS案例自{DATE}起转入CFCO（县级资助县级运营）项目。",
            "Ձեր IHSS գործը {DATE}-ից փոխանցվում է CFCO ծրագրին:",
            true));

        all.add(msg("FS05", "FS", "Hours Increase - Program Transfer",
            "As a result of your transfer to {PROGRAM}, your authorized hours have increased from {HOURS} to {HOURS} per month effective {DATE}.",
            "Como resultado de su transferencia a {PROGRAM}, sus horas autorizadas han aumentado de {HOURS} a {HOURS} por mes a partir del {DATE}.",
            "由于您转入{PROGRAM}，您的授权小时数自{DATE}起从每月{HOURS}增加到{HOURS}。",
            "Ձեր {PROGRAM} փոխանցման արդյունքում Ձեր թույլատրված ժամերն ավելացել են {HOURS}-ից {HOURS} ամսական {DATE}-ից:",
            true));

        all.add(msg("FS06", "FS", "Hours Decrease - Program Transfer",
            "As a result of your transfer to {PROGRAM}, your authorized hours have decreased from {HOURS} to {HOURS} per month effective {DATE}.",
            "Como resultado de su transferencia a {PROGRAM}, sus horas autorizadas han disminuido de {HOURS} a {HOURS} por mes a partir del {DATE}.",
            "由于您转入{PROGRAM}，您的授权小时数自{DATE}起从每月{HOURS}减少到{HOURS}。",
            "Ձեր {PROGRAM} փոխանցման արդյունքում Ձեր թույլատրված ժամերը նվազել են {HOURS}-ից {HOURS} ամսական {DATE}-ից:",
            true));

        // FS07-FS24 abbreviated entries
        for (int i = 7; i <= 24; i++) {
            all.add(msg(String.format("FS%02d", i), "FS",
                "Funding Source Change " + i,
                "Your IHSS funding source or program has changed effective {DATE}. Please contact your county office for details.",
                "Su fuente de financiamiento o programa de IHSS ha cambiado a partir del {DATE}.",
                "您的IHSS资金来源或计划自{DATE}起发生了变化。",
                "Ձեր IHSS ֆինանսավորման աղբյուրը կամ ծրագիրը փոխվել է {DATE}-ից:",
                true));
        }

        // ─────────────────────────────────────────────────────────────────────
        // HR — Hours Reassessment (Group HR)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("HR01", "HR", "Hours Reassessment - No Change",
            "Your IHSS hours reassessment has been completed. There is no change to your authorized hours of {HOURS} per month. This determination is effective {DATE}.",
            "Su reevaluación de horas de IHSS ha sido completada. No hay cambio en sus horas autorizadas de {HOURS} por mes. Esta determinación es efectiva el {DATE}.",
            "您的IHSS小时重新评估已完成。您每月{HOURS}小时的授权小时数没有变化。此决定自{DATE}起生效。",
            "Ձեր IHSS ժամերի վերգնահատումն ավարտվել է: Ձեր ամսական {HOURS} թույլատրված ժամերում փոփոխություն չի լինի: Այս որոշումն ուժի մեջ է {DATE}-ից:",
            true));

        all.add(msg("HR02", "HR", "Hours Reassessment - Some Change",
            "Your IHSS hours reassessment has been completed. Changes have been made to your authorized hours effective {DATE}. Please review this notice for details.",
            "Su reevaluación de horas de IHSS ha sido completada. Se han realizado cambios en sus horas autorizadas a partir del {DATE}.",
            "您的IHSS小时重新评估已完成。您的授权小时数自{DATE}起已发生变化。",
            "Ձեր IHSS ժամերի վերգնահատումն ավարտվել է: Ձեր թույլատրված ժամերում փոփոխություններ են կատարվել {DATE}-ից:",
            true));

        all.add(msg("HR03", "HR", "Hours Reassessment - Increase",
            "Your IHSS hours reassessment has resulted in an increase of your authorized hours. Effective {DATE}, your authorized hours will increase from {HOURS} to {HOURS} per month.",
            "Su reevaluación de horas de IHSS ha resultado en un aumento de sus horas autorizadas. A partir del {DATE}, sus horas autorizadas aumentarán de {HOURS} a {HOURS} por mes.",
            "您的IHSS小时重新评估导致您的授权小时数增加。自{DATE}起，您的授权小时数将从每月{HOURS}增加到{HOURS}。",
            "Ձեր IHSS ժամերի վերգնահատումն հանգեցրել է Ձեր թույլատրված ժամերի ավելացման: {DATE}-ից Ձեր թույլատրված ժամերն ավելանում են {HOURS}-ից {HOURS} ամսական:",
            true));

        all.add(msg("HR04", "HR", "Hours Reassessment - Decrease",
            "Your IHSS hours reassessment has resulted in a decrease of your authorized hours. Effective {DATE}, your authorized hours will decrease from {HOURS} to {HOURS} per month.",
            "Su reevaluación de horas de IHSS ha resultado en una disminución de sus horas autorizadas. A partir del {DATE}, sus horas autorizadas disminuirán de {HOURS} a {HOURS} por mes.",
            "您的IHSS小时重新评估导致您的授权小时数减少。自{DATE}起，您的授权小时数将从每月{HOURS}减少到{HOURS}。",
            "Ձեր IHSS ժամերի վերգնահատումն հանգեցրել է Ձեր թույլատրված ժամերի կրճատման: {DATE}-ից Ձեր թույլատրված ժամերը նվազում են {HOURS}-ից {HOURS} ամսական:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // IN — Legal Services Notice (Group IN)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("IN01", "IN", "Legal Services Notice",
            "If you disagree with this decision, you may request a State Hearing. Free legal assistance may be available from your local legal aid organization. Contact your county IHSS office for referral information.",
            "Si no está de acuerdo con esta decisión, puede solicitar una Audiencia Estatal. Puede haber asistencia legal gratuita disponible de su organización local de ayuda legal.",
            "如果您不同意此决定，您可以申请国家听证会。您当地的法律援助机构可能提供免费法律援助。",
            "Եթե համաձայն չեք այս որոշման հետ, կարող եք պահանջել Նահանգային Լսում: Կարող է հասանելի լինել անվճար իրավական օգնություն:",
            false));

        // ─────────────────────────────────────────────────────────────────────
        // LM — Legislative Mandate Reduction (Group LM)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("LM01", "LM", "Legislative Mandate - Domestic Services Reduction",
            "Due to a change in state law, your authorized hours for domestic services are being reduced effective {DATE}. Your domestic service hours will be reduced from {HOURS} to {HOURS} per month.",
            "Debido a un cambio en la ley estatal, sus horas autorizadas para servicios domésticos se reducen a partir del {DATE}.",
            "由于州法律的变化，您的家庭服务授权小时数自{DATE}起减少。",
            "Նահանգային օրենքի փոփոխության պատճառով Ձեր կենցաղային ծառայությունների թույլատրված ժամերը կրճատվում են {DATE}-ից:",
            true));

        all.add(msg("LM02", "LM", "Legislative Mandate - Meal Services Reduction",
            "Due to a legislative mandate, your authorized hours for meal preparation and meal cleanup services are being reduced effective {DATE}.",
            "Debido a un mandato legislativo, sus horas autorizadas para los servicios de preparación de comidas y limpieza se reducen a partir del {DATE}.",
            "由于立法命令，您的餐食准备和清理服务授权小时数自{DATE}起减少。",
            "Օրենսդրական պահանջի պատճառով Ձեր սննդի պատրաստման ծառայությունների թույլատրված ժամերը կրճատվում են {DATE}-ից:",
            true));

        all.add(msg("LM03", "LM", "Legislative Mandate - Related Services Reduction",
            "Due to a legislative mandate, your authorized hours for related services are being reduced effective {DATE}.",
            "Debido a un mandato legislativo, sus horas autorizadas para servicios relacionados se reducen a partir del {DATE}.",
            "由于立法命令，您的相关服务授权小时数自{DATE}起减少。",
            "Օրենսդրական պահանջի պատճառով Ձեր հարակից ծառայությունների թույլատրված ժամերը կրճատվում են {DATE}-ից:",
            true));

        all.add(msg("LM04", "LM", "Legislative Mandate - Accompaniment Services Reduction",
            "Due to a legislative mandate, your authorized hours for accompaniment services are being reduced effective {DATE}.",
            "Debido a un mandato legislativo, sus horas autorizadas para los servicios de acompañamiento se reducen a partir del {DATE}.",
            "由于立法命令，您的陪同服务授权小时数自{DATE}起减少。",
            "Օրենսդրական պահանջի պատճառով Ձեր ուղեկցման ծառայությունների թույլատրված ժամերը կրճատվում են {DATE}-ից:",
            true));

        all.add(msg("LM05", "LM", "Legislative Mandate - Multiple Services Reduction",
            "Due to a legislative mandate, your authorized hours for the following services are being reduced effective {DATE}: {SERVICES}.",
            "Debido a un mandato legislativo, sus horas autorizadas para los siguientes servicios se reducen a partir del {DATE}: {SERVICES}.",
            "由于立法命令，以下服务的授权小时数自{DATE}起减少：{SERVICES}。",
            "Օրենսդրական պահանջի պատճառով հետևյալ ծառայությունների Ձեր թույլատրված ժամերը կրճատվում են {DATE}-ից: {SERVICES}:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // LV — Leave / Not Currently Residing in Own Home (Group LV)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("LV01", "LV", "Leave - Hospitalized",
            "Your IHSS services are being suspended effective {DATE} because you are hospitalized. Your services will be reinstated when you return home. Please notify your county office when you are discharged.",
            "Sus servicios de IHSS se suspenden a partir del {DATE} porque está hospitalizado(a). Sus servicios serán reinstalados cuando regrese a casa.",
            "由于您住院，您的IHSS服务自{DATE}起暂停。当您回家后，您的服务将恢复。",
            "Ձեր IHSS ծառայությունները կասեցվում են {DATE}-ից, քանի որ Դուք հոսպիտալացված եք: Ձեր ծառայությունները կվերականգնվեն, երբ Դուք վերադառնաք տուն:",
            true));

        all.add(msg("LV02", "LV", "Leave - Skilled Nursing Facility",
            "Your IHSS services are being suspended effective {DATE} because you are residing in a Skilled Nursing Facility. Your services will be reinstated when you return to your own home.",
            "Sus servicios de IHSS se suspenden a partir del {DATE} porque reside en una Instalación de Enfermería Especializada.",
            "由于您居住在专业护理机构，您的IHSS服务自{DATE}起暂停。",
            "Ձեր IHSS ծառայությունները կասեցվում են {DATE}-ից, քանի որ Դուք ապրում եք Մասնագիտական բուժքույրական կենտրոնում:",
            true));

        all.add(msg("LV03", "LV", "Leave - Intermediate Care Facility",
            "Your IHSS services are being suspended effective {DATE} because you are residing in an Intermediate Care Facility.",
            "Sus servicios de IHSS se suspenden a partir del {DATE} porque reside en una Instalación de Cuidado Intermedio.",
            "由于您居住在中级护理机构，您的IHSS服务自{DATE}起暂停。",
            "Ձեր IHSS ծառայությունները կասեցվում են {DATE}-ից, քանի որ Դուք ապրում եք Միջանկյալ բուժօգնության հաստատությունում:",
            true));

        all.add(msg("LV04", "LV", "Leave - Community Care Facility",
            "Your IHSS services are being suspended effective {DATE} because you are residing in a Community Care Facility.",
            "Sus servicios de IHSS se suspenden a partir del {DATE} porque reside en una Instalación de Cuidado Comunitario.",
            "由于您居住在社区护理机构，您的IHSS服务自{DATE}起暂停。",
            "Ձեր IHSS ծառայությունները կասեցվում են {DATE}-ից, քանի որ Դուք ապրում եք Համայնքային խնամքի հաստատությունում:",
            true));

        all.add(msg("LV05", "LV", "Leave - Resources Disposed",
            "Your IHSS case has been placed on leave effective {DATE} because you have disposed of resources that resulted in your disqualification from IHSS.",
            "Su caso de IHSS ha sido puesto en licencia a partir del {DATE} porque ha dispuesto de recursos que resultaron en su descalificación de IHSS.",
            "您的IHSS案例自{DATE}起被暂停，因为您处置了导致您失去IHSS资格的资源。",
            "Ձեր IHSS գործը {DATE}-ից դրվել է արձակուրդի, քանի որ Դուք կատարել եք ռեսուրսների տնօրինում, ինչը հանգեցրել է IHSS-ից Ձեր ոչ պիտանելիությանը:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // MS — Medical Social Worker Service (Group MS)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("MS01", "MS", "Medical Social Worker Service Authorized",
            "Medical social worker services have been authorized for your IHSS case effective {DATE}.",
            "Los servicios de trabajador social médico han sido autorizados para su caso de IHSS a partir del {DATE}.",
            "医疗社会工作者服务已于{DATE}起为您的IHSS案例授权。",
            "Բժշկական սոցիալ աշխատողի ծառայությունները թույլատրվել են Ձեր IHSS գործի համար {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // PM — Protective Monitoring (Group PM)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("PM01", "PM", "Protective Monitoring Authorized",
            "Protective monitoring services have been authorized for your IHSS case effective {DATE}. Authorized hours: {HOURS} per month.",
            "Los servicios de monitoreo protector han sido autorizados para su caso de IHSS a partir del {DATE}.",
            "保护性监护服务已于{DATE}起为您的IHSS案例授权。",
            "Պաշտպանիչ մոնիտորինգի ծառայությունները թույլատրվել են Ձեր IHSS գործի համար {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // PS — Protective Supervision (Group PS)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("PS01", "PS", "Protective Supervision Authorized",
            "Protective supervision services have been authorized for your IHSS case effective {DATE}. Authorized hours: {HOURS} per month.",
            "Los servicios de supervisión protectora han sido autorizados para su caso de IHSS a partir del {DATE}.",
            "保护性监督服务已于{DATE}起为您的IHSS案例授权。",
            "Պաշտպանիչ վերահսկողության ծառայությունները թույլատրվել են Ձեր IHSS գործի համար {DATE}-ից:",
            true));

        all.add(msg("PS02", "PS", "Protective Supervision Denied",
            "Protective supervision services have been denied for your IHSS case because the medical certification does not support the need for this service.",
            "Los servicios de supervisión protectora han sido denegados para su caso de IHSS porque la certificación médica no respalda la necesidad de este servicio.",
            "由于医疗证明不支持此服务的需求，您的IHSS案例的保护性监督服务已被拒绝。",
            "Ձեր IHSS գործի համար պաշտպանիչ վերահսկողության ծառայությունները մերժվել են, քանի որ բժշկական վկայագիրն չի հաստատում այս ծառայության կարիքը:",
            false));

        // ─────────────────────────────────────────────────────────────────────
        // RH — Respite Hours (Group RH)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("RH01", "RH", "Respite Hours Authorized",
            "Respite hours have been authorized for your IHSS case effective {DATE}. Authorized respite hours: {HOURS} per month.",
            "Las horas de respiro han sido autorizadas para su caso de IHSS a partir del {DATE}.",
            "临时照护小时数已于{DATE}起为您的IHSS案例授权。",
            "Հանգստի ժամերը թույլատրվել են Ձեր IHSS գործի համար {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // RS — Rescind (Group RS)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("RS01", "RS", "Rescind - State Hearing Filed",
            "The termination/denial action on your IHSS case has been rescinded because you filed a State Hearing request before the action became effective. Your IHSS services will continue at the previously authorized level.",
            "La acción de terminación/denegación de su caso de IHSS ha sido rescindida porque presentó una solicitud de Audiencia Estatal antes de que la acción entrara en vigor.",
            "您的IHSS案例的终止/拒绝行动已被撤销，因为您在行动生效之前提出了国家听证会请求。",
            "Ձեր IHSS գործի դադարեցման/մերժման գործողությունը վերացվել է, քանի որ Դուք Նահանգային Լսման հայտ ներկայացրեցիք մինչ գործողությունն ուժի մեջ մտնելը:",
            false));

        all.add(msg("RS02", "RS", "Rescind - Recipient Request",
            "The termination action on your IHSS case has been rescinded at your request. Your IHSS services will continue at the previously authorized level effective {DATE}.",
            "La acción de terminación de su caso de IHSS ha sido rescindida a su solicitud. Sus servicios de IHSS continuarán al nivel previamente autorizado a partir del {DATE}.",
            "应您的请求，您的IHSS案例的终止行动已被撤销。您的IHSS服务将自{DATE}起以先前授权的水平继续。",
            "Ձեր IHSS գործի դադարեցման գործողությունը վերացվել է Ձեր հայտի հիման վրա: Ձեր IHSS ծառայությունները կշարունակվեն {DATE}-ից:",
            true));

        all.add(msg("RS03", "RS", "Rescind - Administrative Error",
            "The action on your IHSS case has been rescinded because it was taken in error. Your IHSS services will continue at the previously authorized level effective {DATE}.",
            "La acción en su caso de IHSS ha sido rescindida porque se tomó por error. Sus servicios de IHSS continuarán al nivel previamente autorizado a partir del {DATE}.",
            "由于错误，您的IHSS案例的行动已被撤销。您的IHSS服务将自{DATE}起以先前授权的水平继续。",
            "Ձեր IHSS գործի վրա ձեռնարկված գործողությունը վերացվել է, քանի որ այն կատարվել է սխալմամբ: Ձեր IHSS ծառայությունները կշարունակվեն {DATE}-ից:",
            true));

        all.add(msg("RS04", "RS", "Rescind - State Hearing Decision",
            "A State Hearing decision has been issued in your favor. The action on your IHSS case has been rescinded and your services will be restored effective {DATE}.",
            "Se ha emitido una decisión de Audiencia Estatal a su favor. La acción en su caso de IHSS ha sido rescindida y sus servicios serán restaurados a partir del {DATE}.",
            "已作出有利于您的国家听证会决定。您的IHSS案例的行动已被撤销，您的服务将自{DATE}起恢复。",
            "Նահանգային Լսումի որոշումը կայացվել է Ձեր օգտին: Ձեր IHSS գործի վրա ձեռնարկված գործողությունը վերացվել է, և ծառայությունները կվերականգնվեն {DATE}-ից:",
            true));

        all.add(msg("RS05", "RS", "Rescind - Medi-Cal Non-Compliance Resolved",
            "Your Medi-Cal non-compliance issue has been resolved. The termination action on your IHSS case has been rescinded and your services will be restored effective {DATE}.",
            "Su problema de incumplimiento de Medi-Cal ha sido resuelto. La acción de terminación en su caso de IHSS ha sido rescindida y sus servicios serán restaurados a partir del {DATE}.",
            "您的Medi-Cal不合规问题已解决。您的IHSS案例的终止行动已被撤销，您的服务将自{DATE}起恢复。",
            "Ձեր Medi-Cal-ի չհամապատասխանության հարցը լուծվել է: Ձեր IHSS գործի դադարեցման գործողությունը վերացվել է, և ծառայությունները կվերականգնվեն {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // SC — Share of Cost (Group SC)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("SC01", "SC", "Share of Cost - Established",
            "Based on your income, a Medi-Cal Share of Cost has been established for your case effective {DATE}. Your monthly Share of Cost is {AMOUNT}. This amount must be met before Medi-Cal will pay for services.",
            "Basándose en sus ingresos, se ha establecido una Participación en los Costos de Medi-Cal para su caso a partir del {DATE}. Su Participación Mensual en los Costos es de {AMOUNT}.",
            "根据您的收入，自{DATE}起为您的案例建立了Medi-Cal费用分摊。您的月度费用分摊金额为{AMOUNT}。",
            "Ձեր եկամուտի հիման վրա Ձեր գործի համար {DATE}-ից սահմանվել է Medi-Cal-ի ծախսերի մասնաբաժին: Ձեր ամսական ծախսերի մասնաբաժինն է {AMOUNT}:",
            true));

        all.add(msg("SC02", "SC", "Share of Cost - Increased",
            "Your Medi-Cal Share of Cost has increased effective {DATE}. Your new monthly Share of Cost is {AMOUNT}, increased from {AMOUNT}.",
            "Su Participación en los Costos de Medi-Cal ha aumentado a partir del {DATE}. Su nueva Participación Mensual en los Costos es de {AMOUNT}.",
            "您的Medi-Cal费用分摊自{DATE}起增加。您的新月度费用分摊金额为{AMOUNT}。",
            "Ձեր Medi-Cal-ի ծախսերի մասնաբաժինն ավելացել է {DATE}-ից: Ձեր նոր ամսական ծախսերի մասնաբաժինն է {AMOUNT}:",
            true));

        all.add(msg("SC03", "SC", "Share of Cost - Decreased",
            "Your Medi-Cal Share of Cost has decreased effective {DATE}. Your new monthly Share of Cost is {AMOUNT}, decreased from {AMOUNT}.",
            "Su Participación en los Costos de Medi-Cal ha disminuido a partir del {DATE}. Su nueva Participación Mensual en los Costos es de {AMOUNT}.",
            "您的Medi-Cal费用分摊自{DATE}起减少。您的新月度费用分摊金额为{AMOUNT}。",
            "Ձեր Medi-Cal-ի ծախսերի մասնաբաժինը նվազել է {DATE}-ից: Ձեր նոր ամսական ծախսերի մասնաբաժինն է {AMOUNT}:",
            true));

        all.add(msg("SC04", "SC", "Share of Cost - Eliminated",
            "Your Medi-Cal Share of Cost has been eliminated effective {DATE}. You no longer have a Share of Cost obligation.",
            "Su Participación en los Costos de Medi-Cal ha sido eliminada a partir del {DATE}. Ya no tiene una obligación de Participación en los Costos.",
            "您的Medi-Cal费用分摊自{DATE}起已取消。您不再有费用分摊义务。",
            "Ձեր Medi-Cal-ի ծախսերի մասնաբաժինը վերացվել է {DATE}-ից: Դուք այլևս ծախսերի մասնաբաժնի պարտավորություն չունեք:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // SD — Service Discontinuation (Group SD)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("SD01", "SD", "Service Discontinued - No Longer Needed",
            "Your IHSS service for {SERVICES} has been discontinued effective {DATE} because you no longer have a need for this service.",
            "Su servicio de IHSS para {SERVICES} ha sido discontinuado a partir del {DATE} porque ya no tiene necesidad de este servicio.",
            "由于您不再需要此服务，您针对{SERVICES}的IHSS服务已于{DATE}起停止。",
            "Ձեր {SERVICES}-ի IHSS ծառայությունը դադարեցվել է {DATE}-ից, քանի որ Դուք այլևս կարիք չունեք այս ծառայության:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // SH — State Hearing (Group SH)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("SH01", "SH", "State Hearing - Right to Request",
            "You have the right to request a State Hearing if you disagree with this decision. You must request the hearing within 90 days of the date of this notice. To request a hearing, contact the California Department of Social Services.",
            "Usted tiene el derecho de solicitar una Audiencia Estatal si no está de acuerdo con esta decisión. Debe solicitar la audiencia dentro de los 90 días de la fecha de este aviso.",
            "如果您不同意此决定，您有权申请国家听证会。您必须在本通知日期后90天内申请听证会。",
            "Դուք իրավունք ունեք պահանջել Նահանգային Լսում, եթե համաձայն չեք այս որոշման հետ: Հարցումը պետք է ներկայացնել այս ծանուցման ամսաթվից 90 օրվա ընթացքում:",
            false));

        all.add(msg("SH02", "SH", "State Hearing - Aid Paid Pending",
            "You have requested a State Hearing. If your hearing request is received before the effective date of this action, your current level of IHSS services will continue (aid paid pending) until the hearing decision is issued.",
            "Ha solicitado una Audiencia Estatal. Si su solicitud de audiencia se recibe antes de la fecha efectiva de esta acción, su nivel actual de servicios de IHSS continuará hasta que se emita la decisión de audiencia.",
            "您已申请国家听证会。如果您的听证会请求在此行动生效日期之前收到，您当前的IHSS服务水平将继续（待决援助），直到发出听证会决定。",
            "Դուք պահանջել եք Նահանգային Լսում: Եթե Ձեր լսման հայտն ստացվի մինչ այս գործողության ուժի մեջ մտնելու ամսաթիվը, IHSS ծառայությունների Ձեր ընթացիկ մակարդակը կշարունակվի:",
            false));

        all.add(msg("SH03", "SH", "State Hearing - Decision in Favor",
            "A State Hearing decision has been issued in your favor. Your IHSS services will be restored to the level indicated in the hearing decision effective {DATE}.",
            "Se ha emitido una decisión de Audiencia Estatal a su favor. Sus servicios de IHSS serán restaurados al nivel indicado en la decisión de audiencia a partir del {DATE}.",
            "已作出有利于您的国家听证会决定。您的IHSS服务将自{DATE}起恢复到听证会决定中指示的水平。",
            "Կայացվել է Ձեր օգտին Նահանգային Լսումի որոշում: Ձեր IHSS ծառայությունները կվերականգնվեն {DATE}-ից:",
            true));

        all.add(msg("SH04", "SH", "State Hearing - Decision Against",
            "A State Hearing decision has been issued. The decision upholds the action taken on your IHSS case. This notice will serve as your final determination.",
            "Se ha emitido una decisión de Audiencia Estatal. La decisión confirma la acción tomada en su caso de IHSS.",
            "已作出国家听证会决定。该决定支持对您的IHSS案例采取的行动。",
            "Կայացվել է Նահանգային Լսումի որոշում: Որոշումը հաստատում է Ձեր IHSS գործի վրա ձեռնարկված գործողությունը:",
            false));

        all.add(msg("SH05", "SH", "State Hearing - Filed Before Termination",
            "A State Hearing has been filed on your behalf before the effective date of the termination. Your IHSS services will continue until the hearing decision is issued.",
            "Se ha presentado una Audiencia Estatal en su nombre antes de la fecha efectiva de la terminación. Sus servicios de IHSS continuarán hasta que se emita la decisión de la audiencia.",
            "在终止生效日期之前，已代表您提交了国家听证会。您的IHSS服务将继续，直到发出听证会决定。",
            "Դադարեցման ուժի մեջ մտնելու ամսաթվից առաջ Ձեր անունից ներկայացվել է Նահանգային Լսում: Ձեր IHSS ծառայությունները կշարունակվեն մինչ լսումի որոշումը:",
            false));

        // ─────────────────────────────────────────────────────────────────────
        // SP — Special Circumstances (Group SP)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("SP01", "SP", "Special Circumstances - Paramedical Services",
            "Paramedical services have been authorized for your IHSS case effective {DATE}. These services require a physician's written order and medical certification.",
            "Los servicios paramédicos han sido autorizados para su caso de IHSS a partir del {DATE}.",
            "辅助医疗服务已于{DATE}起为您的IHSS案例授权。这些服务需要医生的书面命令和医疗证明。",
            "Պարաբժշկական ծառայությունները թույլատրվել են Ձեր IHSS գործի համար {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // TR — Termination Reasons (Group TR)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("TR01", "TR", "Termination - Left California",
            "Your IHSS services will be terminated effective {DATE} because you are no longer a resident of California.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque ya no es residente de California.",
            "由于您不再是加利福尼亚州居民，您的IHSS服务将于{DATE}起终止。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ Դուք այլևս Կալիֆոռնիայի բնակիչ չեք:",
            true));

        all.add(msg("TR02", "TR", "Termination - No Longer in Own Home",
            "Your IHSS services will be terminated effective {DATE} because you are no longer living in your own home.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque ya no vive en su propio hogar.",
            "由于您不再住在自己的家里，您的IHSS服务将于{DATE}起终止。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ Դուք այլևս ապրում եք ոչ Ձեր սեփական տանը:",
            true));

        all.add(msg("TR03", "TR", "Termination - No Longer Medi-Cal Eligible",
            "Your IHSS services will be terminated effective {DATE} because you are no longer eligible for Medi-Cal.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque ya no es elegible para Medi-Cal.",
            "由于您不再符合Medi-Cal资格，您的IHSS服务将于{DATE}起终止。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ Դուք այլևս Medi-Cal-ի իրավունք չունեք:",
            true));

        all.add(msg("TR04", "TR", "Termination - Death",
            "This case is being terminated effective {DATE} due to the death of the recipient.",
            "Este caso se está terminando a partir del {DATE} debido al fallecimiento del beneficiario.",
            "此案例自{DATE}起因受益人去世而终止。",
            "Այս գործը դադարեցվում է {DATE}-ից՝ շահառուի մահվան պատճառով:",
            true));

        all.add(msg("TR05", "TR", "Termination - No Functional Need",
            "Your IHSS services will be terminated effective {DATE} because the reassessment indicates you no longer have a functional need for IHSS services.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque la reevaluación indica que ya no tiene una necesidad funcional de los servicios de IHSS.",
            "由于重新评估表明您不再在功能上需要IHSS服务，您的IHSS服务将于{DATE}起终止。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ վերգնահատումը ցույց է տալիս, որ Դուք այլևս ֆունկցիոնալ կարիք չունեք IHSS ծառայությունների:",
            true));

        all.add(msg("TR06", "TR", "Termination - Medi-Cal Non-Compliance",
            "Your IHSS services will be terminated effective {DATE} because you have not complied with your Medi-Cal requirements. Your services may be reinstated if you comply with Medi-Cal requirements within 90 days.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque no ha cumplido con sus requisitos de Medi-Cal. Sus servicios pueden ser reinstalados si cumple con los requisitos de Medi-Cal dentro de los 90 días.",
            "由于您未遵守Medi-Cal要求，您的IHSS服务将于{DATE}起终止。如果您在90天内遵守Medi-Cal要求，您的服务可能会恢复。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ Դուք չեք կատարել Medi-Cal-ի պահանջները:",
            true));

        all.add(msg("TR07", "TR", "Termination - Recipient Request",
            "Your IHSS services will be terminated effective {DATE} at your request.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} a su solicitud.",
            "应您的请求，您的IHSS服务将于{DATE}起终止。",
            "Ձեռ IHSS ծառայությունները կդադարեցվեն {DATE}-ից Ձեր հայտի հիման վրա:",
            true));

        all.add(msg("TR08", "TR", "Termination - Whereabouts Unknown",
            "Your IHSS services will be terminated effective {DATE} because your whereabouts are unknown.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque se desconoce su paradero.",
            "由于您的下落不明，您的IHSS服务将于{DATE}起终止。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ Ձեր գտնվելու վայրն անհայտ է:",
            true));

        all.add(msg("TR09", "TR", "Termination - Income Exceeds Limit",
            "Your IHSS services will be terminated effective {DATE} because your income exceeds the IHSS eligibility limit.",
            "Sus servicios de IHSS serán terminados a partir del {DATE} porque sus ingresos superan el límite de elegibilidad de IHSS.",
            "由于您的收入超过IHSS资格限制，您的IHSS服务将于{DATE}起终止。",
            "Ձեր IHSS ծառայությունները կդադարեցվեն {DATE}-ից, քանի որ Ձեր եկամուտը գերազանցում է IHSS-ի իրավունքի սահմանաչափը:",
            true));

        all.add(msg("TR10", "TR", "Termination - Administrative Error Correction",
            "The previous IHSS authorization was issued in error. Services will be terminated effective {DATE} to correct the administrative error.",
            "La autorización previa de IHSS fue emitida por error. Los servicios serán terminados a partir del {DATE} para corregir el error administrativo.",
            "之前的IHSS授权是错误发出的。服务将于{DATE}起终止以纠正行政错误。",
            "Նախկին IHSS թույլտվությունը տրվել է սխալմամբ: Ծառայությունները կդադարեցվեն {DATE}-ից վարչական սխալը ուղղելու համար:",
            true));

        all.add(msg("TR18", "TR", "Termination - Administrative Error Rescind",
            "A previous administrative action was taken in error. This notice is to inform you that the error has been corrected effective {DATE}.",
            "Se tomó una acción administrativa anterior por error. Este aviso es para informarle que el error ha sido corregido a partir del {DATE}.",
            "之前的行政行动是错误采取的。本通知旨在告知您该错误已于{DATE}起得到纠正。",
            "Նախկին վարչական գործողությունը ձեռնարկվել է սխալմամբ: Սույն ծանուցումը Ձեզ տեղեկացնում է, որ սխալն ուղղվել է {DATE}-ից:",
            true));

        all.add(msg("TR25", "TR", "Termination - Medi-Cal Compliance Block",
            "Your IHSS case was terminated for Medi-Cal non-compliance (CC514) within the past 90 days. Reactivation is not available at this time. Please contact your county IHSS office.",
            "Su caso de IHSS fue terminado por incumplimiento de Medi-Cal (CC514) dentro de los últimos 90 días. La reactivación no está disponible en este momento.",
            "您的IHSS案例在过去90天内因Medi-Cal不合规（CC514）而终止。目前无法重新激活。",
            "Ձեր IHSS գործը դադարեցվել է Medi-Cal-ի չհամապատասխանության (CC514) պատճառով վերջին 90 օրվա ընթացքում: Վերակտիվացումը ներկայումս հնարավոր չէ:",
            false));

        all.add(msg("TR26", "TR", "Termination - Medi-Cal Non-Compliance Resolved",
            "Your Medi-Cal non-compliance has been resolved. Your IHSS services will be restored effective {DATE}.",
            "Su incumplimiento de Medi-Cal ha sido resuelto. Sus servicios de IHSS serán restaurados a partir del {DATE}.",
            "您的Medi-Cal不合规已得到解决。您的IHSS服务将自{DATE}起恢复。",
            "Ձեր Medi-Cal-ի չհամապատասխանությունը լուծվել է: Ձեր IHSS ծառայությունները կվերականգնվեն {DATE}-ից:",
            true));

        // ─────────────────────────────────────────────────────────────────────
        // VS — Various / Miscellaneous (Group VS)
        // ─────────────────────────────────────────────────────────────────────
        all.add(msg("VS01", "VS", "Continuation of IHSS Services",
            "Your IHSS services will continue at the authorized level of {HOURS} per month effective {DATE}.",
            "Sus servicios de IHSS continuarán al nivel autorizado de {HOURS} por mes a partir del {DATE}.",
            "您的IHSS服务将自{DATE}起以每月{HOURS}的授权水平继续。",
            "Ձեր IHSS ծառայությունները կշարունակվեն {HOURS} ամսական թույլատրված մակարդակով {DATE}-ից:",
            true));

        all.add(msg("VS02", "VS", "Multi-Program Enrollment",
            "Your case has been enrolled in multiple IHSS programs effective {DATE}. Please review this notice for the services authorized under each program.",
            "Su caso ha sido inscrito en múltiples programas de IHSS a partir del {DATE}.",
            "您的案例自{DATE}起已在多个IHSS项目中注册。",
            "Ձեր գործը {DATE}-ից ընդգրկվել է IHSS-ի բազմաթիվ ծրագրերում:",
            true));

        all.add(msg("VS03", "VS", "Inter-County Transfer",
            "Your IHSS case is being transferred from {COUNTY} County to {COUNTY} County effective {DATE}. Your services will continue without interruption.",
            "Su caso de IHSS está siendo transferido del Condado de {COUNTY} al Condado de {COUNTY} a partir del {DATE}.",
            "您的IHSS案例自{DATE}起从{COUNTY}县转移到{COUNTY}县。您的服务将不中断地继续。",
            "Ձեր IHSS գործը {DATE}-ից փոխանցվում է {COUNTY} կոմսությունից {COUNTY} կոմսություն:",
            true));

        repository.saveAll(all);
        log.info("NOA category messages seeded: {} records", all.size());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static NoaCategoryMessageEntity msg(
            String code, String group, String title,
            String en, String es, String zh, String hy,
            boolean hasVars) {
        NoaCategoryMessageEntity m = new NoaCategoryMessageEntity();
        m.setCategoryCode(code);
        m.setCategoryGroup(group);
        m.setTitle(title);
        m.setTextEnglish(en);
        m.setTextSpanish(es);
        m.setTextChinese(zh);
        m.setTextArmenian(hy);
        m.setHasVariables(hasVars);
        return m;
    }
}
