package com.mycorp;

import com.mycorp.support.Poliza;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import util.datos.UsuarioAlta;

import static org.mockito.ArgumentMatchers.any;


/**
 * Unit test for simple App.
 */
public class RealizarSimulacionTest extends TestCase {


    private ZendeskService sut;


    private RestTemplate restTemplateMock;

    @Before
    public void prepare(){


    }

    /**
     * Rigourous Test :-)
     */
    @Test
    public void testApp() {
        assertTrue( true );
    }


    @Test
    public void testAltaTicket(){

        this.sut = new ZendeskService();
        this.restTemplateMock = Mockito.mock(RestTemplate.class);
        this.sut.setRestTemplate(this.restTemplateMock);

        UsuarioAlta usuarioAlta = new UsuarioAlta();
        usuarioAlta.setNumTarjeta("12312312");
        usuarioAlta.setNumPoliza("2854");
        String userAgent = "TEST";

        ResponseEntity responseMock = Mockito.mock(ResponseEntity.class);

        Mockito.when(this.restTemplateMock.getForEntity(any(String.class), any(Class.class))).thenReturn( responseMock);
        Mockito.when(responseMock.getStatusCode()).thenReturn( HttpStatus.OK);
        Mockito.when(responseMock.getBody()).thenReturn( "Algo");

        String response = this.sut.altaTicketZendesk(usuarioAlta, userAgent);

        assertNotNull(response);

    }


    @Test
    public void testDatosPoliza() {

        this.sut = new ZendeskService();

        Poliza pol = this.sut.setDatosPoliza(new Integer(1), new Integer(2));
        assertNotNull(pol);

    }

    @Test
    public void testAnadirDatosFormulario() {

        this.sut = new ZendeskService();
        UsuarioAlta usuarioAlta = new UsuarioAlta();


        StringBuilder stb = this.sut.anadirDatosForm(usuarioAlta, any(String.class));
        assertNotNull(stb);

    }

    @Test
    public void testAnadirDatosFormularioUsuarioNull() {

        this.sut = new ZendeskService();
        UsuarioAlta usuarioAlta = new UsuarioAlta();
        usuarioAlta.setNumPoliza("987987");


        StringBuilder stb = this.sut.anadirDatosForm(usuarioAlta, any(String.class));
        assertNotNull(stb);

    }

    @Test
    public void testAnadirDatosBravo() {

        this.sut = new ZendeskService();

        StringBuilder stb = this.sut.anadirDatosBravo(any(String.class));
        assertNotNull(stb);

    }


    @Test(expected = ZendeskException.class)
    public void testShouldZendeskException() throws Exception{


        this.sut = new ZendeskService();
        this.restTemplateMock = Mockito.mock(RestTemplate.class);
        this.sut.setRestTemplate(this.restTemplateMock);

        Mockito.when(this.restTemplateMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException());
        Mockito.when(this.restTemplateMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException("ZendeskException"));
        Mockito.when(this.restTemplateMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException("ZendeskException", new Throwable()));
        Mockito.when(this.restTemplateMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException(new Throwable()));

        this.restTemplateMock.getForEntity(any(String.class), any(Class.class));

    }


}
