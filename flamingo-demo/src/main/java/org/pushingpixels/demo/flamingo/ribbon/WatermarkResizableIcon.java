/*
 * Copyright (c) 2005-2018 Flamingo / Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.demo.flamingo.ribbon;

import org.pushingpixels.neon.NeonCortex;
import org.pushingpixels.neon.icon.ResizableIcon;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.SubstanceCortex.ComponentOrParentChainScope;
import org.pushingpixels.substance.api.colorscheme.*;
import org.pushingpixels.substance.api.watermark.SubstanceWatermark;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.*;

/**
 * Resizable icon for <b>Substance </b> watermarks.
 *
 * @author Kirill Grouchnikov
 */
public class WatermarkResizableIcon implements ResizableIcon {
    /**
     * Current width in pixels.
     */
    private int currWidth;

    /**
     * Current height in pixels.
     */
    private int currHeight;

    /**
     * Associated watermark. Can be <code>null</code>.
     */
    private SubstanceWatermark watermark;

    /**
     * Images for water signs.
     */
    private static Map<Integer, BufferedImage> waterSigns = new HashMap<Integer, BufferedImage>();

    /**
     * Creates a new icon.
     *
     * @param watermark   Associated watermark (can be <code>null</code>).
     * @param startWidth  Original width in pixels.
     * @param startHeight Original heigth in pixels.
     */
    public WatermarkResizableIcon(SubstanceWatermark watermark, int startWidth, int startHeight) {
        this.watermark = watermark;
        this.currWidth = startWidth;
        this.currHeight = startHeight;
    }

    @Override
    public void setDimension(Dimension newDimension) {
        this.currWidth = newDimension.width;
        this.currHeight = newDimension.height;
    }

    @Override
    public int getIconHeight() {
        return this.currHeight;
    }

    @Override
    public int getIconWidth() {
        return this.currWidth;
    }

    /**
     * Returns a watermark sign image.
     *
     * @param size Image size.
     * @return Watermark sign image.
     */
    private static BufferedImage getWatermarkSign(int size) {
        if (waterSigns.containsKey(size))
            return waterSigns.get(size);

        BufferedImage blurred = NeonCortex.getBlankImage(size, size);
        Font font = new Font("Tahoma", size > 15 ? Font.PLAIN : Font.BOLD, size);
        Graphics2D blurredGraphics = (Graphics2D) blurred.getGraphics().create();
        blurredGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        blurredGraphics.setColor(new Color(255, 255, 255, 96));
        blurredGraphics.fillRect(0, 0, size, size);
        blurredGraphics.setColor(Color.white);
        blurredGraphics.setFont(font);
        blurredGraphics.drawString("\u2248", 1, size - 1);

        float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f,
                0.0625f };
        Kernel kernel = new Kernel(3, 3, data);

        ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage result = NeonCortex.getBlankImage(size, size);
        convolve.filter(blurred, result);
        Graphics2D graphics = (Graphics2D) result.getGraphics().create();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(new Color(0, 0, 255));
        graphics.setFont(font);
        graphics.drawString("\u2248", 1, size - 1);

        blurredGraphics.dispose();
        // graphics.dispose();

        waterSigns.put(size, result);
        return result;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        float borderThickness = 1.0f / (float) NeonCortex.getScaleFactor();
        graphics.setStroke(new BasicStroke(borderThickness, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND));

        graphics.translate(x, y);
        graphics.clipRect(0, 0, this.currWidth, this.currHeight);
        if (this.watermark != null) {
            graphics.setColor(SubstanceCortex.ComponentScope.getCurrentSkin(c)
                    .getEnabledColorScheme(ComponentOrParentChainScope.getDecorationType(c))
                    .getExtraLightColor());
            graphics.fillRect(0, 0, this.currWidth, this.currHeight);
            Composite oldComp = graphics.getComposite();
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

            SubstanceSkin metallicSkin = new SubstanceSkin() {
                @Override
                public String getDisplayName() {
                    return "Metallic Skin / Watermark";
                }
            };
            metallicSkin.registerDecorationAreaSchemeBundle(
                    new SubstanceColorSchemeBundle(new MetallicColorScheme(),
                            new MetallicColorScheme(),
                            new LightGrayColorScheme()),
                    SubstanceSlices.DecorationAreaType.NONE);

            this.watermark.previewWatermark(graphics, metallicSkin,
                    0, 0, this.currWidth, this.currHeight);
            graphics.setComposite(oldComp);

            int waterSize = this.currHeight / 2;
            BufferedImage watermarkSign = getWatermarkSign(waterSize);
            NeonCortex.drawImage(graphics, watermarkSign, this.currWidth - waterSize - 2,
                    this.currHeight - waterSize - 2);
        } else {
            int waterSize = Math.min(this.currHeight, this.currWidth) - 2;
            BufferedImage watermarkSign = getWatermarkSign(waterSize);
            NeonCortex.drawImage(graphics, watermarkSign, (this.currWidth - waterSize) / 2,
                    (this.currHeight - waterSize) / 2);
        }
        graphics.setColor(Color.black);
        graphics.draw(new Rectangle2D.Float(0, 0,
                this.currWidth, //- borderThickness,
                this.currHeight));// - borderThickness));

        graphics.dispose();
    }
}
