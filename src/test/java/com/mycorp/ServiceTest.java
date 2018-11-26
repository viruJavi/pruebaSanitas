package com.mycorp;

import com.mycorp.support.DatosCliente;
import com.mycorp.support.ValueCode;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import util.datos.UsuarioAlta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class ServiceTest extends TestCase {


    private ZendeskService sut;
    private ZendeskService sut2;


    private RestTemplate restTemplateMock;
    private RestTemplate restTemplateMock2;



    @Test
    public void testUsuarioAltaVacio() {

        this.sut2 = new ZendeskService();
        this.restTemplateMock2 = Mockito.mock(RestTemplate.class);
        this.sut2.setRestTemplate(this.restTemplateMock2);

        UsuarioAlta usuarioAlta = new UsuarioAlta();
        usuarioAlta.setNumTarjeta("");
        usuarioAlta.setNumPoliza("");
        String userAgent = "";

        ResponseEntity responseMock = Mockito.mock(ResponseEntity.class);

        Mockito.when(this.restTemplateMock2.getForEntity(any(String.class), eq(String.class))).thenReturn( responseMock);
        Mockito.when(responseMock.getStatusCode()).thenReturn( HttpStatus.OK);
        Mockito.when(responseMock.getBody()).thenReturn( "Algo");

        String response = this.sut2.altaTicketZendesk(usuarioAlta, userAgent);

        assertNotNull(response);


    }

    @Test
    public void testAnadirDatos() {

        this.sut = new ZendeskService();
        this.restTemplateMock = Mockito.mock(RestTemplate.class);
        this.sut.setRestTemplate(this.restTemplateMock);

        StringBuilder stb = new StringBuilder();
        DatosCliente datosMock = Mockito.mock(DatosCliente.class);


        //Mockito.when(this.sut.anadirDatosBravo(any(String.class))).thenReturn( stringBuilder);
        Mockito.when(this.restTemplateMock.getForObject(any(String.class), eq(DatosCliente.class), any(String.class))).thenReturn( datosMock);

        stb = this.sut.anadirDatosBravo(any(String.class));

        assertNotNull(stb);
    }


}
