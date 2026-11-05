package com.grupo14.hito2.controllers;

import com.grupo14.hito2.dtos.CrearPedidoDTO;
import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.entities.Pedido;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.services.*;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/empleado")
@RequiredArgsConstructor
public class EmpleadoController {

    private final CuentaService cuentaService;
    private final LiquidacionService liquidacionService;
    private final PedidoService pedidoService;
    private final UnidadVentaService unidadVentaService;
    private final PDFService pdfService;
    private final PlatoService platoService;

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        Personal personal = cuentaService.buscarPorEmail(principal.getName());
        model.addAttribute("empleado", personal);
        model.addAttribute("puedeCargarPedidos", personal.puedeCargarPedidos());
        model.addAttribute("unidadActual", unidadVentaService.obtenerUnidadEnFestivalEnCurso(personal.getId()).orElse(null));
        model.addAttribute("recibos", liquidacionService.obtenerRecibos(principal.getName()));
        return "empleado/empleadoIndex";
    }

    @GetMapping("/historial")
    public String verHistorial(Model model, Principal principal) {
        long personalId = cuentaService.buscarPorEmail(principal.getName()).getId();
        model.addAttribute("unidades", unidadVentaService.obtenerHistorial(personalId));
        return "empleado/historial";
    }

    // Ver no exige festival en curso: muestra los pedidos de su unidad más reciente
    @GetMapping("/pedidos")
    public String verPedidos(Model model, Principal principal) {
        long personalId = cuentaService.buscarPorEmail(principal.getName()).getId();
        UnidadVenta unidad = unidadVentaService.buscarPorStaff(personalId);
        model.addAttribute("unidad", unidad);
        model.addAttribute("pedidos", pedidoService.obtenerPedidosDeUnidad(unidad.getId()));
        return "empleado/pedidos";
    }

    // Solo el cajero: lo vendido hoy por la unidad del festival en curso y los cierres anteriores
    @GetMapping("/cerrar-caja")
    public String verCierreDeCaja(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Personal cajero = validarQueSeaCajero(principal);
        try {
            UnidadVenta unidad = unidadEnCurso(cajero);
            model.addAttribute("cierre", pedidoService.calcularCierreDelDia(unidad));
            model.addAttribute("cierresAnteriores", pedidoService.obtenerCierresDeUnidad(unidad));
            return "empleado/cerrar-caja";
        } catch (NoEncontradoException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/perfil";
        }
    }

    @PostMapping("/cerrar-caja")
    public String cerrarCaja(Principal principal, RedirectAttributes redirectAttributes) {
        Personal cajero = validarQueSeaCajero(principal);
        try {
            pedidoService.cerrarCaja(unidadEnCurso(cajero), cajero);
            redirectAttributes.addFlashAttribute("mensaje", "Caja cerrada: no se cargan más pedidos hasta mañana");
        } catch (NoEncontradoException | ReglaDeNegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/cerrar-caja";
    }

    @GetMapping("/recibos")
    public String verRecibos(Model model, Principal principal) {
        model.addAttribute("recibos", liquidacionService.obtenerRecibos(principal.getName()));
        return "empleado/recibos";
    }

    // Solo se venden pedidos en la unidad del festival en curso
    @GetMapping("/pedidos/crear")
    public String crearPedidoForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Personal cajero = validarQueSeaCajero(principal);
        UnidadVenta unidad;
        try {
            unidad = unidadEnCurso(cajero);
        } catch (NoEncontradoException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + ": no podés cargar pedidos");
            return "redirect:/empleado/perfil";
        }
        if (pedidoService.cajaCerradaHoy(unidad)) {
            redirectAttributes.addFlashAttribute("error", "La caja de hoy ya está cerrada: no se cargan más pedidos hasta mañana");
            return "redirect:/empleado/perfil";
        }
        model.addAttribute("unidad", unidad);
        // Solo platos activos: los dados de baja no se ofrecen en nuevos pedidos
        model.addAttribute("platos", platoService.obtenerPlatosActivosPorUnidad(unidad.getId()));
        model.addAttribute("crearPedidoDTO", new CrearPedidoDTO());
        return "empleado/crear-pedido";
    }

    @PostMapping("/pedidos/crear")
    public String crearPedido(@ModelAttribute CrearPedidoDTO dto, Principal principal, RedirectAttributes redirectAttributes) {
        Personal cajero = validarQueSeaCajero(principal);
        try {
            Pedido pedido = pedidoService.crearPedido(dto, unidadEnCurso(cajero));
            redirectAttributes.addFlashAttribute("mensaje", "Pedido creado exitosamente. ID: " + pedido.getId());
            return "redirect:/empleado/perfil";
        } catch (NoEncontradoException | ReglaDeNegocioException e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el pedido: " + e.getMessage());
            return "redirect:/empleado/pedidos/crear";
        }
    }

    // Solo de festivales donde trabajó: si no, ErrorHandler muestra el 404
    @GetMapping("/recibos/descargar/{festivalId}")
    public ResponseEntity<byte[]> descargarReciboPDF(@PathVariable long festivalId, Principal principal) throws IOException {
        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(principal.getName(), festivalId);
        return DescargaPdf.de(pdfService.generarReciboPDF(recibo), "recibo_" + festivalId + ".pdf");
    }

    // Solo se venden pedidos en la unidad del festival en curso
    private UnidadVenta unidadEnCurso(Personal personal) {
        return unidadVentaService.buscarUnidadEnFestivalEnCurso(personal.getId());
    }

    // Polimórfico: cada tipo de personal sabe si opera la caja (cargar pedidos y cerrarla)
    private Personal validarQueSeaCajero(Principal principal) {
        Personal personal = cuentaService.buscarPorEmail(principal.getName());
        if (!personal.puedeCargarPedidos()) {
            throw new AccessDeniedException("Solo los cajeros pueden cargar pedidos y cerrar la caja");
        }
        return personal;
    }
}
