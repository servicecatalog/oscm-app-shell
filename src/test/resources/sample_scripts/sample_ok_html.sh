#!/bin/bash

echo '
    {"status":"ok",
     "message":"Script execution is successful",
     "data":{
        "output":"this is valid <b>html data</b> and table<br/><br/>
                <p style='font-size:30px\;color:\#4169E1'>some text</p>
                <table style='border-style:solid\;border-width:5px\;background-color:\#4CAF50\;padding:5px\;width:70%'>
                    <tr>
                        <td>Column_1</td><td>Column_2</td>
                    </tr>
                    <tr>
                        <td>Val1</td><td>Val2</td>
                    </tr>
                </table>"
        }
     }'

echo "END_OF_SCRIPT";
