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
    private ZendeskService sutException;


    private RestTemplate restTemplateMock;
    private RestTemplate restTemplateExceptionMock;

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


    @Test(expected = ZendeskException.class)
    public void testShouldZendeskException() throws Exception{


        this.sutException = new ZendeskService();
        this.restTemplateExceptionMock = Mockito.mock(RestTemplate.class);
        this.sutException.setRestTemplate(this.restTemplateExceptionMock);

        Mockito.when(this.restTemplateExceptionMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException());
        Mockito.when(this.restTemplateExceptionMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException("ZendeskException"));
        Mockito.when(this.restTemplateExceptionMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException("ZendeskException", new Throwable()));
        Mockito.when(this.restTemplateExceptionMock.getForEntity(any(String.class), any(Class.class))).thenThrow(new ZendeskException(new Throwable()));

        this.restTemplateExceptionMock.getForEntity(any(String.class), any(Class.class));

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



}
