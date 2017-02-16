/* Copyright (c) 2014 - 2017 Bradley Justice
MIT LICENSE
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.blueverdi.rosietheriveter;


public class AppInfo
{
    /**
     * The login parameters should be specified in the following manner:
     * 
     * public static final String SpeechKitServer = "ndev.server.name";
     * 
     * public static final int SpeechKitPort = 1000;
     * 
     * public static final String SpeechKitAppId = "ExampleSpeechKitSampleID";
     * 
     * public static final byte[] SpeechKitApplicationKey =
     * {
     *     (byte)0x38, (byte)0x32, (byte)0x0e, (byte)0x46, (byte)0x4e, (byte)0x46, (byte)0x12, (byte)0x5c, (byte)0x50, (byte)0x1d,
     *     (byte)0x4a, (byte)0x39, (byte)0x4f, (byte)0x12, (byte)0x48, (byte)0x53, (byte)0x3e, (byte)0x5b, (byte)0x31, (byte)0x22,
     *     (byte)0x5d, (byte)0x4b, (byte)0x22, (byte)0x09, (byte)0x13, (byte)0x46, (byte)0x61, (byte)0x19, (byte)0x1f, (byte)0x2d,
     *     (byte)0x13, (byte)0x47, (byte)0x3d, (byte)0x58, (byte)0x30, (byte)0x29, (byte)0x56, (byte)0x04, (byte)0x20, (byte)0x33,
     *     (byte)0x27, (byte)0x0f, (byte)0x57, (byte)0x45, (byte)0x61, (byte)0x5f, (byte)0x25, (byte)0x0d, (byte)0x48, (byte)0x21,
     *     (byte)0x2a, (byte)0x62, (byte)0x46, (byte)0x64, (byte)0x54, (byte)0x4a, (byte)0x10, (byte)0x36, (byte)0x4f, (byte)0x64
     * };
     * 
     * Please note that all the specified values are non-functional
     * and are provided solely as an illustrative example.
     * 
     */

    /* Please contact Nuance to receive the necessary connection and login parameters */
    public static final String SpeechKitServer = "cua.nmdp.nuancemobility.net"/* Enter your server here */;

    public static final int SpeechKitPort = 443/* Enter your port here */;
    
    public static final boolean SpeechKitSsl = false;

    public static final String SpeechKitAppId = "NMDPPRODUCTION_blueVerdi_Rosie_the_Riveter_20141120181847"/* Enter your ID here */;

    public static final byte[] SpeechKitApplicationKey = {
        /* Enter your application key here:
        (byte)0x00, (byte)0x01, ... (byte)0x00
        */
    	(byte)0xf7, (byte)0xa5, (byte)0x4a, (byte)0x64, (byte)0x99, (byte)0xd5, (byte)0x8b, (byte)0xbf, (byte)0xbd, (byte)0xc0, (byte)0x77, (byte)0xe6, (byte)0xdd, (byte)0xca, (byte)0x56,
    	(byte)0xaf, (byte)0xaf, (byte)0x14, (byte)0x12, (byte)0x74, (byte)0x2e, (byte)0x4d, (byte)0xca, (byte)0x20, (byte)0x03, (byte)0x72, (byte)0x5a, (byte)0x49, (byte)0x40, (byte)0x2e,
    	(byte)0xa8, (byte)0x37, (byte)0xd3, (byte)0xf2, (byte)0x9c, (byte)0x6d, (byte)0xc8, (byte)0x27, (byte)0x2c, (byte)0x85, (byte)0xe7, (byte)0xa3, (byte)0x6b, (byte)0xc4, (byte)0x6d,
    	(byte)0xc2, (byte)0x74, (byte)0x1c, (byte)0xd6, (byte)0x86, (byte)0x91, (byte)0x04, (byte)0xd3, (byte)0x5b, (byte)0x24, (byte)0xd6, (byte)0xcd, (byte)0x7e, (byte)0x20, (byte)0x0d,
    	(byte)0xac, (byte)0xc8, (byte)0x45, (byte)0x80
    };
}