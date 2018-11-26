package com.mycorp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.mycorp.propiedades.ZendeskProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mycorp.support.CorreoElectronico;
import com.mycorp.support.DatosCliente;
import com.mycorp.support.MensajeriaService;
import com.mycorp.support.Poliza;
import com.mycorp.support.PolizaBasicoFromPolizaBuilder;
import com.mycorp.support.Ticket;
import com.mycorp.support.ValueCode;

import portalclientesweb.ejb.interfaces.PortalClientesWebEJBRemote;
import util.datos.PolizaBasico;
import util.datos.UsuarioAlta;

@Service
public class ZendeskService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger( ZendeskService.class );

    private static final String ESCAPED_LINE_SEPARATOR = "\\n";
    private static final String ESCAPE_ER = "\\n";
    private static final String HTML_BR = "<br/>";
    @Value("#{envPC['zendesk.ticket']}")
    public String PETICION_ZENDESK= "";

    @Value("#{envPC['zendesk.token']}")
    public String TOKEN_ZENDESK= "";

    @Value("#{envPC['zendesk.url']}")
    public String URL_ZENDESK= "";

    @Value("#{envPC['zendesk.user']}")
    public String ZENDESK_USER= "";

    @Value("#{envPC['tarjetas.getDatos']}")
    public String TARJETAS_GETDATOS = "";

    @Value("#{envPC['cliente.getDatos']}")
    public String CLIENTE_GETDATOS = "";

    @Value("#{envPC['zendesk.error.mail.funcionalidad']}")
    public String ZENDESK_ERROR_MAIL_FUNCIONALIDAD = "";

    @Value("#{envPC['zendesk.error.destinatario']}")
    public String ZENDESK_ERROR_DESTINATARIO = "";

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


    /** The portalclientes web ejb remote. */
    @Autowired
    // @Qualifier("portalclientesWebEJB")
    private PortalClientesWebEJBRemote portalclientesWebEJBRemote;

    /** The rest template. */
    @Autowired
    @Qualifier("restTemplateUTF8")
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier( "emailService" )
    MensajeriaService emailService;

    private final String CONST_POTENCIAL = "POTENCIAL";
    private final String CONST_REAL = "REAL";
    private final String CONST_PROSPECTO = "PROSPECTO";

    private final int POTENCIAL = 1;
    private final int REAL = 2;
    private final int PROSPECTO = 3;

    /**
     * Crea un ticket en Zendesk. Si se ha informado el nÂº de tarjeta, obtiene los datos asociados a dicha tarjeta de un servicio externo.
     * @param usuarioAlta
     * @param userAgent
     */
    public String altaTicketZendesk(UsuarioAlta usuarioAlta, String userAgent){

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String idCliente = null;

        StringBuilder clientName = new StringBuilder();


        // AÃ±ade los datos del formulario
        StringBuilder datosUsuario = anadirDatosForm(usuarioAlta, userAgent);


        StringBuilder datosServicio = new StringBuilder();
        // Obtiene el idCliente de la tarjeta
        if(StringUtils.isNotBlank(usuarioAlta.getNumTarjeta())){
            try{
                String urlToRead = TARJETAS_GETDATOS + usuarioAlta.getNumTarjeta();
                ResponseEntity<String> res = restTemplate.getForEntity( urlToRead, String.class);
                if(res.getStatusCode() == HttpStatus.OK){
                    String dusuario = res.getBody();
                    clientName.append(dusuario);
                    idCliente = dusuario;
                    datosServicio.append("Datos recuperados del servicio de tarjeta:").append(ESCAPED_LINE_SEPARATOR).append(mapper.writeValueAsString(dusuario));
                }
            }catch(Exception e)
            {
                LOG.error("Error al obtener los datos de la tarjeta", e);
            }
        }
        else if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
            try
            {
                Poliza poliza = setDatosPoliza(Integer.valueOf(usuarioAlta.getNumPoliza()), Integer.valueOf(usuarioAlta.getNumDocAcreditativo()));

                PolizaBasico polizaBasicoConsulta = new PolizaBasicoFromPolizaBuilder().withPoliza( poliza ).build();

                final util.datos.DetallePoliza detallePolizaResponse = portalclientesWebEJBRemote.recuperarDatosPoliza(polizaBasicoConsulta);

                clientName.append(detallePolizaResponse.getTomador().getNombre()).
                            append(" ").
                            append(detallePolizaResponse.getTomador().getApellido1()).
                            append(" ").
                            append(detallePolizaResponse.getTomador().getApellido2());

                idCliente = detallePolizaResponse.getTomador().getIdentificador();
                datosServicio.append("Datos recuperados del servicio de tarjeta:").append(ESCAPED_LINE_SEPARATOR).append(mapper.writeValueAsString(detallePolizaResponse));
            }catch(Exception e)
            {
                LOG.error("Error al obtener los datos de la poliza", e);
            }
        }

        StringBuilder datosBravo = anadirDatosBravo(idCliente);


        String ticket = String.format(PETICION_ZENDESK, clientName.toString(), usuarioAlta.getEmail(), datosUsuario.toString()+datosBravo.toString()+
                parseJsonBravo(datosServicio));
        ticket = ticket.replaceAll("["+ESCAPED_LINE_SEPARATOR+"]", " ");

        try(Zendesk zendesk = new Zendesk.Builder(URL_ZENDESK).setUsername(ZENDESK_USER).setToken(TOKEN_ZENDESK).build()){
            //Ticket
            Ticket petiZendesk = mapper.readValue(ticket, Ticket.class);
            zendesk.createTicket(petiZendesk);

        }catch(Exception e){
            LOG.error("Error al crear ticket ZENDESK", e);
            // Send email

            CorreoElectronico correo = new CorreoElectronico( Long.parseLong(ZENDESK_ERROR_MAIL_FUNCIONALIDAD), "es" )
                    .addParam(datosUsuario.toString().replaceAll(ESCAPE_ER+ESCAPED_LINE_SEPARATOR, HTML_BR))
                    .addParam(datosBravo.toString().replaceAll(ESCAPE_ER+ESCAPED_LINE_SEPARATOR, HTML_BR));
            correo.setEmailA( ZENDESK_ERROR_DESTINATARIO );
            try
            {
                emailService.enviar( correo );
            }catch(Exception ex){
                LOG.error("Error al enviar mail", ex);
            }

        }

        datosUsuario.append(datosBravo);

        return datosUsuario.toString();
    }

    private StringBuilder anadirDatosBravo(String idCliente){

        StringBuilder datosBravo = new StringBuilder();

        datosBravo.append(ESCAPED_LINE_SEPARATOR + "Datos recuperados de BRAVO:" + ESCAPED_LINE_SEPARATOR + ESCAPED_LINE_SEPARATOR);

        try
        {
            // Obtenemos los datos del cliente
            DatosCliente cliente = restTemplate.getForObject("http://localhost:8080/test-endpoint", DatosCliente.class, idCliente);

            datosBravo.append("TelÃ©fono: ").append(cliente.getGenTGrupoTmk()).append(ESCAPED_LINE_SEPARATOR);


            datosBravo.append("Feha de nacimiento: ").append(formatter.format(formatter.parse(cliente.getFechaNacimiento()))).append(ESCAPED_LINE_SEPARATOR);

            List< ValueCode > tiposDocumentos = getTiposDocumentosRegistro();
            for(int i = 0; i < tiposDocumentos.size();i++)
            {
                if(tiposDocumentos.get(i).getCode().equals(cliente.getGenCTipoDocumento().toString()))
                {
                    datosBravo.append("Tipo de documento: ").append(tiposDocumentos.get(i).getValue()).append(ESCAPED_LINE_SEPARATOR);
                }
            }
            datosBravo.append("NÃºmero documento: ").append(cliente.getNumeroDocAcred()).append(ESCAPED_LINE_SEPARATOR);

            datosBravo.append("Tipo cliente: ");
            switch (cliente.getGenTTipoCliente()) {
                case POTENCIAL:
                    datosBravo.append(CONST_POTENCIAL).append(ESCAPED_LINE_SEPARATOR);
                    break;
                case REAL:
                    datosBravo.append(CONST_REAL).append(ESCAPED_LINE_SEPARATOR);
                    break;
                case PROSPECTO:
                    datosBravo.append(CONST_PROSPECTO).append(ESCAPED_LINE_SEPARATOR);
                    break;
            }

            datosBravo.append("ID estado del cliente: ").append(cliente.getGenTStatus()).append(ESCAPED_LINE_SEPARATOR);

            datosBravo.append("ID motivo de alta cliente: ").append(cliente.getIdMotivoAlta()).append(ESCAPED_LINE_SEPARATOR);

            datosBravo.append("Registrado: ").append((cliente.getfInactivoWeb() == null ? "SÃ­" : "No")).append(ESCAPED_LINE_SEPARATOR + ESCAPED_LINE_SEPARATOR);

        }catch(Exception e)
        {
            LOG.error("Error al obtener los datos en BRAVO del cliente", e);
        }

        return datosBravo;


    }

    private StringBuilder anadirDatosForm(UsuarioAlta usuarioAlta, String userAgent){

        StringBuilder datosUsuario = new StringBuilder();

        if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
            datosUsuario.append(ZendeskProperties.getnPolizas()).append(usuarioAlta.getNumPoliza()).append("/").append(usuarioAlta.getNumDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
        }else{
            datosUsuario.append(ZendeskProperties.getnTarj()).append(usuarioAlta.getNumTarjeta()).append(ESCAPED_LINE_SEPARATOR);
        }
        datosUsuario.append(ZendeskProperties.getTipoDoc()).append(usuarioAlta.getTipoDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
        datosUsuario.append(ZendeskProperties.getnDoc()).append(usuarioAlta.getNumDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
        datosUsuario.append(ZendeskProperties.getEmail()).append(usuarioAlta.getEmail()).append(ESCAPED_LINE_SEPARATOR);
        datosUsuario.append(ZendeskProperties.getnMov()).append(usuarioAlta.getNumeroTelefono()).append(ESCAPED_LINE_SEPARATOR);
        datosUsuario.append(ZendeskProperties.getUser()).append(userAgent).append(ESCAPED_LINE_SEPARATOR);

        return datosUsuario;

    }

    private Poliza setDatosPoliza(Integer numPoliza, Integer numColectivo){

        Poliza poliza = new Poliza();
        poliza.setNumPoliza(numPoliza);
        poliza.setNumColectivo(numColectivo);
        poliza.setCompania(1);

        return poliza;
    }

    public List< ValueCode > getTiposDocumentosRegistro() {
        return Arrays.asList( new ValueCode(), new ValueCode() ); // simulacion servicio externo
    }

    /**
     * MÃ©todo para parsear el JSON de respuesta de los servicios de tarjeta/pÃ³liza
     *
     * @param resBravo
     * @return
     */
    private String parseJsonBravo(StringBuilder resBravo)
    {
        return resBravo.toString().replaceAll("[\\[\\]\\{\\}\\\"\\r]", "").replaceAll(ESCAPED_LINE_SEPARATOR, ESCAPE_ER + ESCAPED_LINE_SEPARATOR);
    }
}