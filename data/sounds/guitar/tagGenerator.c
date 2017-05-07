#include <stdio.h>
#include <math.h>

int main()
{
    int i;
    double x = pow(2, 1.0 / 12);
    char str[] = 
"    {\n"
"        \"startTimeInMS\": %d,\n"
"        \"endTimeInMS\": %d,\n"
"        \"frequency\": %lf,\n"
"        \"extendable\": false,\n"
"        \"extStartTimeInMS\": 0,\n"
"        \"extEndTimeInMS\": 0\n"
"    },\n";
    for (i = 0; i < 61; ++i)
    {
        printf(str, i * 2603, (i + 1) * 2603, 440.0 * pow(x, i - 33));
    }
}